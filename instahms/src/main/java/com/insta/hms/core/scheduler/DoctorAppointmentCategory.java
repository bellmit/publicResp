package com.insta.hms.core.scheduler;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.hospitalcenters.HospitalCenterService;
import com.insta.hms.mdm.practitionerconsultationmapping.PractitionerConsultationMappingService;
import com.insta.hms.mdm.testequipments.TestEquipmentService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class DoctorAppointmentCategory.
 */
@Component
public class DoctorAppointmentCategory implements AppointmentCategory {

  /** The scheduler resource types repository. */
  @LazyAutowired
  private SchedulerResourceTypesRepository schedulerResourceTypesRepository;

  /** The doctor service. */
  @LazyAutowired
  private DoctorService doctorService;

  /** The equipment service. */
  @LazyAutowired
  private TestEquipmentService equipmentService;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The hospital center service. */
  @LazyAutowired
  private HospitalCenterService hospitalCenterService;

  /** The consultation types service. */
  @LazyAutowired
  private ConsultationTypesService consultationTypesService;
  
  /** The practitioner consultation mapping service. */
  @LazyAutowired
  private PractitionerConsultationMappingService practitionerConsultationMappingService;
  
  @LazyAutowired
  private AdditionalResourcesQueryProvider additionalResourcesQueryProvider;

  /** The Constant category. */
  private static final String category = ResourceCategory.DOC.name();

  /** The Constant PRIMARY_RESOURCE_TYPE. */
  private static final String PRIMARY_RESOURCE_TYPE = "OPDOC";

  /** The Constant SECONDARY_RESOURCE_TYPE. */
  private static final String SECONDARY_RESOURCE_TYPE = "CTY";

  /** The Constant SECONDARY_REOSURCE_MASTER_QUERY. */
  // TODO : change this to consultation type
  private static final String SECONDARY_REOSURCE_MASTER_QUERY = " SELECT distinct "
      + "d.doctor_id as resource_id,"
      + " d.doctor_name as resource_name,d.overbook_limit,'DOC'::text as resource_type, "
      + " '' as scheduler_resource_type, d.available_for_online_consults,"
      + " d.doctor_mobile as contact_number,dept.dept_id,"
      + "dept.dept_name" + " FROM doctors d "
      + " LEFT JOIN department dept ON(dept.dept_id=d.dept_id)"
      + " LEFT JOIN doctor_center_master dcm on (d.doctor_id = dcm.doctor_id)"
      + " WHERE d.schedule = true AND d.status='A' AND dcm.status = 'A' #CENTER_FILTER#"
      + " #RESOURCE_FILTER# #DEPT_FILTER#";

  /** The Constant PRIMARY_REOSURCE_MASTER_QUERY. */
  private static final String PRIMARY_REOSURCE_MASTER_QUERY = " SELECT distinct "
      + "d.doctor_id as resource_id,"
      + " d.doctor_name as resource_name,d.overbook_limit,'DOC'::text as resource_type, "
      + " '' as scheduler_resource_type, d.available_for_online_consults, "
      + " d.doctor_mobile as contact_number,dept.dept_id,"
      + " dept.dept_name"
      + " FROM doctors d "
      + " LEFT JOIN department dept ON(dept.dept_id=d.dept_id)"
      + " LEFT JOIN doctor_center_master dcm on (d.doctor_id = dcm.doctor_id)"
      + " WHERE d.schedule = true AND d.status='A' AND dcm.status = 'A' #CENTER_FILTER# "
      + "#RESOURCE_FILTER# #DEPT_FILTER#";

  /** The Constant GET_APPOINTMENT_DETAILS. */
  private static final String GET_APPOINTMENT_DETAILS = "SELECT ap.mr_no,ap.visit_id,"
      + "coalesce(cd.patient_name, pd.patient_name) as patient_name,"
      + "coalesce(cd.last_name,pd.last_name) as last_name, "
      + "coalesce(cd.salutation_name,pd.salutation) as salutation_name,"
      + "ap.patient_contact, ap.complaint,srt.category,"
      + " ap.remarks,ap.scheduler_visit_type,ap.visit_mode,"
      + " coalesce(cd.vip_status,pd.vip_status) as vip_status, ap.presc_doc_id, "
      + "pdoc.doctor_name as presc_doc_name, "
      + "coalesce(cd.patient_dob,pd.dateofbirth) as patient_dob, cd.patient_age, "
      + "cd.patient_age_units, pd.expected_dob,"
      + " case when ap.mr_no is not null then "
      + " age(coalesce(dateofbirth,expected_dob)) :: text else null end as age_text,"
      + " (select doctor_name from doctors where doctor_id=ap.presc_doc_id) as presc_doctor,"
      + " r.dept_id as dept_id, r.dept_name as dept_name, "
      + " coalesce(cd.patient_gender,pd.patient_gender) as patient_gender,"
      + " ap.patient_category, ap.patient_address,"
      + " ap.center_id, ap.scheduler_prior_auth_no,ap.scheduler_prior_auth_mode_id,"
      + " ap.appointment_id,ap.res_sch_id,ap.prim_res_id,ap.res_sch_name, ap.teleconsult_url,"
      + "to_char(ap.appointment_time,'dd-MM-yyyy') AS text_appointment_date, "
      + "ap.practo_appointment_id, "
      + " ap.patient_area, ap.patient_state, ap.patient_city, ap.patient_country, "
      + "ap.patient_nationality, to_char(ap.appointment_time,'dd-MM-yyyy hh24:mi:ss') "
      + "AS text_appointment_date_time, to_char(ap.appointment_time,'hh24:mi')::time "
      + "AS appointment_time, "
      + " coalesce(cd.patient_email_id,pd.email_id) as patient_email_id,"
      + " ap.patient_citizen_id, ap.primary_sponsor_id, "
      + "ap.primary_sponsor_co, ap.plan_id, ap.plan_type_id, ap.member_id, to_char("
      + "ap.appointment_time,'hh24:mi') AS text_appointment_time,appointment_time "
      + "as appointment_date_time, "
      + " to_char(appointment_time+(duration||' mins')::interval,'dd-MM-yyyy hh24:mi:ss')"
      + " AS text_end_appointment_time, "
      + " to_char(appointment_time+(duration||' mins')::interval,'hh24:mi') "
      + "AS text_end_appointment_only_time, ap.duration,ap.appointment_status,ap.arrival_time, "
      + " ap.completed_time,ap.pat_package_id, ap.booked_by,ap.booked_time, "
      + "cas.appointment_source_id, cas.paid_at_source, ap.changed_by,ap.changed_time,"
      + "apit.resource_type, apit.resource_id, "
      + " apit.appointment_item_id,srt.primary_resource, ap.primary_sponsor_id, "
      + "ap.primary_sponsor_id, ap.plan_id, ap.plan_id, ap.member_id, tpa.tpa_name, "
      + " CASE WHEN (cf.receive_communication in ('S','B') OR cf.receive_communication is null)"
      + " then 'Y' else 'N' end as send_sms, "
      + " CASE WHEN (cf.receive_communication in ('E','B')  OR cf.receive_communication is null)"
      + " then 'Y' else 'N' end as send_email, "
      + " coalesce(cf.lang_code, (select contact_pref_lang_code from generic_preferences))"
      + " as lang_code, "
      + " CASE WHEN apit.resource_type ='OPDOC' THEN (select doctor_name from doctors"
      + " where doctor_id=apit.resource_id )"
      + " WHEN apit.resource_type ='DOC' THEN (select doctor_name from doctors "
      + "where doctor_id=apit.resource_id )"
      + " WHEN apit.resource_type ='EQID' THEN (select equipment_name from test_equipment_master"
      + " where eq_id::text=apit.resource_id ) "
      + " ELSE (SELECT generic_resource_name FROM generic_resource_type grt "
      + "          JOIN generic_resource_master grm ON(grm.generic_resource_type_id = "
      + "grt.generic_resource_type_id)"
      + "          AND grm.generic_resource_id::text = apit.resource_id "
      + " WHERE ap.appointment_id = apit.appointment_id AND grt.scheduler_resource_type ="
      + " apit.resource_type)"
      + " END AS resourcename, consultation_type_id  "
      + " FROM scheduler_appointments ap "
      + " LEFT JOIN contact_details cd ON (cd.contact_id = ap.contact_id)"
      + " LEFT JOIN patient_details pd ON (pd.mr_no = ap.mr_no)"
      + " LEFT JOIN scheduler_appointment_items apit USING(appointment_id)"
      + " LEFT JOIN contact_preferences cf on (ap.mr_no = cf.mr_no) "
      + " LEFT JOIN doctors pdoc ON (ap.presc_doc_id = pdoc.doctor_id) "
      + " JOIN ( #MASTER_DATA# ) AS r ON (r.resource_id = ap.prim_res_id) "
      + " LEFT JOIN appointment_source_master cas ON (cas.appointment_source_id = "
      + "ap.app_source_id) "
      + " LEFT JOIN  scheduler_master  sm ON sm.res_sch_id = ap.res_sch_id "
      + " LEFT JOIN scheduler_resource_types srt ON (srt.category = sm.res_sch_category"
      + " AND apit.resource_type = srt.resource_type) "
      + " LEFT JOIN tpa_master tpa ON (tpa.tpa_id = ap.primary_sponsor_id) ";
  
  /**
   * Gets the category.
   *
   * @return the category
   */
  /* (non-Javadoc)
   * @see com.insta.hms.core.scheduler.AppointmentCategory#getCategory()
   */
  @Override
  public String getCategory() {
    return category;

  }

  /**
   * Gets the secondary resource master query.
   *
   * @return the secondary resource master query
   */
  /* (non-Javadoc)
   * @see com.insta.hms.core.scheduler.AppointmentCategory#getSecondaryResourceMasterQuery()
   */
  @Override
  public String getSecondaryResourceMasterQuery() {
    return SECONDARY_REOSURCE_MASTER_QUERY;
  }

  // @Override
  /*
   * public List<List<AppointMentResource>> generateSecondaryResourceList() { // TODO Auto-generated
   * method stub return null; }
   */

  /**
   * Sets the appointment data.
   *
   * @param appointmentsList the appointments list
   * @param params the params
   */
  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.scheduler.AppointmentCategory#setAppointmentData(java.util.List,
   * java.util.Map)
   */
  @SuppressWarnings("unchecked")
  @Override
  public void setAppointmentData(List<Appointment> appointmentsList, Map<String, Object> params) {
    // TODO Auto-generated method stub
    Map<String, Object> appontmentInfo = (Map<String, Object>) params.get("appointment");
    String primResId = appontmentInfo.get("primary_resource_id") != null ? (String) appontmentInfo
        .get("primary_resource_id") : null;
    String schResName = appontmentInfo.get("secondary_resource_id") != null ? String
        .valueOf(appontmentInfo.get("secondary_resource_id")) : null;
    for (Appointment appt : appointmentsList) {
      appt.setConsultationTypeId(Integer.parseInt(schResName));
      appt.setScheduleName(schResName);
      appt.setPrim_res_id(primResId);
    }
  }

  /**
   * Sets the appointment additional resource data.
   *
   * @param appointment the appointment
   * @param appointmentItemsList the appointment items list
   * @param params the params
   */
  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.scheduler.AppointmentCategory#setAppointmentAdditionalResourceData(com.insta
   * .hms.core.scheduler.Appointment, java.util.List, java.util.Map)
   */
  @Override
  public void setAppointmentAdditionalResourceData(Appointment appointment,
      List<AppointmentResource> appointmentItemsList, Map<String, Object> params) {
    String userName = null;
    if (appointment.getPractoAppointmentId() != null
        && !appointment.getPractoAppointmentId().equalsIgnoreCase("")) {
      userName = "InstaAdmin";
    } else {
      userName = (String) sessionService.getSessionAttributes().get("userId");
    }
    Integer appointmentItemId = new GenericRepository("scheduler_appointment_items")
        .getNextSequence();
    Timestamp modTime = DateUtil.getCurrentTimestamp();
    AppointmentResource res = new AppointmentResource(appointment.getAppointmentId(), "OPDOC",
        appointment.getPrim_res_id());
    res.setAppointment_item_id(appointmentItemId);
    res.setUser_name(userName);
    res.setMod_time(modTime);
    appointmentItemsList.add(res);
  }

  /**
   * Gets the resource overbook limit.
   *
   * @param resourceId the resource id
   * @param resType the res type
   * @return the resource overbook limit
   */
  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.scheduler.AppointmentCategory#getResourceOverbookLimit(java.lang.String,
   * java.lang.String)
   */
  @Override
  public Integer getResourceOverbookLimit(String resourceId, String resType) {
    // TODO Auto-generated method stub
    switch (resType) {
      case "DOC":
      case "OPDOC":
        return doctorService.getDoctorOverbookLimit(resourceId);
      case "EQID":
        return equipmentService.getTestEquipmentOverbookLimit(resourceId);
      default:
        return doctorService.getDoctorOverbookLimit(resourceId);
    }
  }

  /**
   * Gets the resource name.
   *
   * @param resourceId the resource id
   * @param resType the res type
   * @return the resource name
   */
  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.scheduler.AppointmentCategory#getResourceName(java.lang.String,
   * java.lang.String)
   */
  @Override
  public String getResourceName(String resourceId, String resType) {
    switch (resType) {
      case "DOC":
      case "OPDOC":
        Map<String, Object> doctor = (Map<String, Object>) doctorService
            .getDoctorDetails(resourceId);
        if (doctor != null) {
          return (String) doctor.get("doctor_name");
        }
        return null;
      case "EQID":
        return equipmentService.getTestEquipmentName(resourceId);
      default:
        return additionalResourcesQueryProvider.getGenericResourceName(resourceId);
    }
  }

  /**
   * Gets the secondary resource type.
   *
   * @return the secondary resource type
   */
  /* (non-Javadoc)
   * @see com.insta.hms.core.scheduler.AppointmentCategory#getSecondaryResourceType()
   */
  @Override
  public String getSecondaryResourceType() {
    return SECONDARY_RESOURCE_TYPE;
  }

  /**
   * Gets the primary resource type.
   *
   * @return the primary resource type
   */
  /* (non-Javadoc)
   * @see com.insta.hms.core.scheduler.AppointmentCategory#getPrimaryResourceType()
   */
  @Override
  public String getPrimaryResourceType() {
    return PRIMARY_RESOURCE_TYPE;
  }

  /**
   * Filter visit timings by center.
   *
   * @param visitTimingsList the visit timings list
   * @param loggedInCenterId the logged in center id
   * @return the list
   */
  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.scheduler.AppointmentCategory#filterVisitTimingsByCenter(java.util.List,
   * int)
   */
  @Override
  public List<BasicDynaBean> filterVisitTimingsByCenter(List<BasicDynaBean> visitTimingsList,
      int loggedInCenterId) {
    for (BasicDynaBean visitTiming : visitTimingsList) {
      if (loggedInCenterId != 0) {
        if (visitTiming.get("availability_status").equals("A")) {
          // if (visitTiming.get("availability_status").equals("A") && !Arrays.asList(0,
          // loggedInCenterId).contains(visitTiming.get("center_id"))) {
          // The available slot is considered as unavailable for the
          // given center, so we mark as 'N'
          int availabilityCenterId = -1;
          if (visitTiming.get("center_id") == null) {
            availabilityCenterId = 0;
          } else {
            availabilityCenterId = (Integer) visitTiming.get("center_id");
          }
          if (availabilityCenterId != 0 && availabilityCenterId != loggedInCenterId) {
            visitTiming.set("availability_status", "N");
          }
        }
      }
    }
    return visitTimingsList;
  }

  /**
   * Gets the appointment details query.
   *
   * @return the appointment details query
   */
  /* (non-Javadoc)
   * @see com.insta.hms.core.scheduler.AppointmentCategory#getAppointmentDetailsQuery()
   */
  @Override
  public String getAppointmentDetailsQuery() {
    String query = GET_APPOINTMENT_DETAILS;
    query = query.replace("#MASTER_DATA#", PRIMARY_REOSURCE_MASTER_QUERY);
    return query;
  }

  /**
   * Gets the primary resource master query.
   *
   * @return the primary resource master query
   */
  /* (non-Javadoc)
   * @see com.insta.hms.core.scheduler.AppointmentCategory#getPrimaryResourceMasterQuery()
   */
  @Override
  public String getPrimaryResourceMasterQuery() {
    return getSecondaryResourceMasterQuery();
  }

  /**
   * Gets the dept filter clause.
   *
   * @return the dept filter clause
   */
  /* (non-Javadoc)
   * @see com.insta.hms.core.scheduler.AppointmentCategory#getDeptFilterClause()
   */
  @Override
  public String getDeptFilterClause() {
    return "AND d.dept_id IN (:deptIds)";
  }

  /**
   * Validate prim res.
   *
   * @return the string
   */
  /* (non-Javadoc)
   * @see com.insta.hms.core.scheduler.AppointmentCategory#validatePrimRes()
   */
  @Override
  public String validatePrimRes() {
    return "select doctor_id from doctors where doctor_id =  ? AND status = 'A'";
  }

  /**
   * Validate sec res.
   *
   * @return the string
   */
  /* (non-Javadoc)
   * @see com.insta.hms.core.scheduler.AppointmentCategory#validateSecRes()
   */
  @Override
  public String validateSecRes() {
    return "select consultation_type_id from consultation_types where consultation_type_id =  "
        + "?::Integer AND status = 'A' AND patient_type = 'o'";
  }

  /** The Constant GET_DURATION_FROM_SEC_RES. */
  public static final String GET_DURATION_FROM_SEC_RES = "select coalesce(duration,15) as duration"
      + " from consultation_types where consultation_type_id = ?";
  
  /** The Constant GET_DURATION_FROM_PRIM_RES. */
  public static final String GET_DURATION_FROM_PRIM_RES = "select default_duration as duration "
      + "from scheduler_master where res_sch_name = ? AND res_sch_category = ? AND status = 'A'";

  /**
   * Gets the appointment duration.
   *
   * @param consultationTypeId the consultation type id
   * @param doctorId the doctor id
   * @return the appointment duration
   */
  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.scheduler.AppointmentCategory#getAppointmentDuration(java.lang.String,
   * java.lang.String)
   */
  @Override
  public int getAppointmentDuration(String consultationTypeId, String doctorId) {
    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(GET_DURATION_FROM_SEC_RES,
        new Object[] { Integer.parseInt(consultationTypeId) });
    int duration = 0;
    if (bean != null) {
      duration = (int) bean.get("duration");
    } else {
      if (doctorId != null) {
        duration = getSlotDurationOfPrimRes(doctorId);
      } else {
        duration = -1;
      }
    }
    return duration;
  }

  /**
   * Gets the slot duration of prim res.
   *
   * @param doctorId the doctor id
   * @return the slot duration of prim res
   */
  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.scheduler.AppointmentCategory#getSlotDurationOfPrimRes(java.lang.String)
   */
  @Override
  public int getSlotDurationOfPrimRes(String doctorId) {
    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(GET_DURATION_FROM_PRIM_RES, new Object[] {
        doctorId, "DOC" });
    int duration = 0;
    if (bean != null) {
      duration = (int) bean.get("duration");
    } else {
      bean = DatabaseHelper
          .queryToDynaBean(GET_DURATION_FROM_PRIM_RES, new Object[] { "*", "DOC" });
      duration = (int) bean.get("duration");
    }
    return duration;
  }

  /** The Constant GET_PRIM_RES. */
  public static final String GET_PRIM_RES = "SELECT d.doctor_id as resource_id, d.doctor_name "
      + "as resource_name FROM doctors d  "
      + "JOIN department dept ON(dept.dept_id=d.dept_id) JOIN doctor_center_master dcm "
      + "on (d.doctor_id = dcm.doctor_id) "
      + "WHERE d.schedule = true AND d.status='A' AND dcm.status = 'A' "
      + "AND dept.dept_id = ? AND dcm.center_id IN (0,?)";

  /**
   * Gets the prim res applicable for sec res.
   *
   * @param secResId the sec res id
   * @param centerId the center id
   * @param deptId the dept id
   * @return the prim res applicable for sec res
   */
  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.scheduler.AppointmentCategory#getPrimResApplicableForSecRes(java.lang.String
   * , int, java.lang.String)
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public List<Map> getPrimResApplicableForSecRes(String secResId, int centerId, String deptId) {

    List<Map> maplist = ConversionUtils.copyListDynaBeansToMap(DatabaseHelper.queryToDynaList(
        GET_PRIM_RES, new Object[] { deptId, centerId }));
    List responseList = new ArrayList();
    for (Map map : maplist) {
      Map tempMap = new HashMap();
      tempMap.putAll(map);
      responseList.add(tempMap);
    }
    return responseList;
  }

  /**
   * Gets the secondary resources.
   *
   * @return the secondary resources
   */
  /* (non-Javadoc)
   * @see com.insta.hms.core.scheduler.AppointmentCategory#getSecondaryResources()
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public List<Map> getSecondaryResources() {
    int centerId = RequestContext.getCenterId();
    BasicDynaBean bean = hospitalCenterService.findByKey(centerId);
    String healthAuth = (String) bean.get("health_authority");
    List<BasicDynaBean> beanList = consultationTypesService.getConsultationTypes("o",
        Arrays.asList(new String[] { "ORG0001" }), healthAuth);
    List<Map> mapList = new ArrayList();
    for (BasicDynaBean consultationTypeBean : beanList) {
      Map map = new HashMap();
      map.putAll(consultationTypeBean.getMap());
      map.put("consultation_type_id", map.get("consultation_type_id").toString());
      mapList.add(map);
    }
    return mapList;
  }
  
  /**
   * Gets the secondary resources.
   *
   * @param doctorId the doctor id
   * @return the secondary resources
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public List<Map> getSecondaryResources(String doctorId) {
    
    Map<String, String> filterMap = new HashMap<>();
    filterMap.put("doctor_id", doctorId);
    BasicDynaBean doctorBean = doctorService.findByPk(filterMap);
    
    Integer practitionerTypeId = (Integer) doctorBean.get("practitioner_id");
    Map responseMap = practitionerConsultationMappingService
        .getDoctorConsultationTypes(practitionerTypeId, "DOC");
    
    List<Map> mapList = (List<Map>) responseMap.get("consultation_types");
    List<Map> resMapList = new ArrayList();
    for (Map consultationTypeMap : mapList) {
      Map map = new HashMap();
      map.putAll(consultationTypeMap);
      map.put("consultation_type_id", map.get("consultation_type_id").toString());
      resMapList.add(map);
    }
    return resMapList;
  }
}

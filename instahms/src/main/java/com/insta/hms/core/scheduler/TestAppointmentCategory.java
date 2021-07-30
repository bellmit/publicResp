package com.insta.hms.core.scheduler;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.testequipments.TestEquipmentService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class TestAppointmentCategory implements AppointmentCategory {

  @LazyAutowired
  private DoctorService doctorService;

  @LazyAutowired
  private TestEquipmentService testEquipmentService;

  @LazyAutowired
  private SessionService sessionService;
  
  @LazyAutowired
  private AdditionalResourcesQueryProvider additionalResourcesQueryProvider;

  private static final String CATEGORY = ResourceCategory.DIA.name();

  private static final String PRIMARY_RESOURCE_TYPE = "EQID";

  private static final String SECONDARY_RESOURCE_TYPE = "DIA";

  // possible bug with department, might get created on making new scheduler screens
  private static final String SECONDARY_REOSURCE_MASTER_QUERY = " SELECT distinct "
      + "d.test_id as resource_id, "
      + " d.test_name as resource_name,'DIA'::text as resource_type, "
      + " '' as scheduler_resource_type, dept.ddept_id,dept.ddept_name"
      + " FROM diagnostics d "
      + " LEFT JOIN diagnostics_departments dept ON(dept.dept_id=d.ddept_id)"
      + " WHERE d.status='A' #CENTER_FILTER# #RESOURCE_FILTER# #DEPT_FILTER#";

  private static final String PRIMARY_REOSURCE_MASTER_QUERY = " SELECT distinct "
      + "tem.eq_id::text as resource_id, "
      + " tem.equipment_name as resource_name,tem.overbook_limit,'EQID'::text "
      + "as resource_type, "
      + " '' as scheduler_resource_type,dept.ddept_id,dept.ddept_name as dept_name"
      + " FROM test_equipment_master tem "
      + " LEFT JOIN diagnostics_departments dept ON(dept.ddept_id=tem.ddept_id)"
      + " WHERE tem.schedule = true AND tem.status='A' #CENTER_FILTER# "
      + "#RESOURCE_FILTER# #DEPT_FILTER#";

  private static final String GET_APPOINTMENT_DETAILS = "SELECT ap.mr_no,ap.visit_id,"
      + "ap.patient_name,ap.patient_contact, ap.complaint,srt.category,"
      + " ap.remarks,ap.scheduler_visit_type, ap.vip_status, ap.presc_doc_id, pdoc.doctor_name "
      + "as presc_doc_name, ap.patient_dob, ap.patient_age, (select doctor_name from doctors"
      + " where doctor_id=ap.presc_doc_id) as presc_doctor, r.dept_id as dept_id, r.dept_name "
      + "as dept_name, "
      + " ap.patient_age_units, ap.patient_gender, ap.patient_category, ap.patient_address, "
      + "ap.center_id, ap.scheduler_prior_auth_no,ap.scheduler_prior_auth_mode_id,"
      + " ap.appointment_id,ap.res_sch_id,ap.res_sch_name, to_char(ap.appointment_time,"
      + "'dd-MM-yyyy') AS text_appointment_date, ap.practo_appointment_id, "
      + " ap.patient_area, ap.patient_state, ap.patient_city, ap.patient_country, "
      + "ap.patient_nationality, to_char(ap.appointment_time,'dd-MM-yyyy hh24:mi:ss') "
      + "AS text_appointment_date_time, to_char(ap.appointment_time,'hh24:mi')::time "
      + "AS appointment_time, "
      + " ap.patient_email_id, ap.patient_citizen_id, ap.primary_sponsor_id, ap.primary_sponsor_co,"
      + " ap.plan_id, ap.plan_type_id, ap.member_id, to_char(ap.appointment_time,'hh24:mi') "
      + "AS text_appointment_time,appointment_time as appointment_date_time, "
      + " to_char(appointment_time+(duration||' mins')::interval,'dd-MM-yyyy hh24:mi:ss') "
      + "AS text_end_appointment_time, "
      + " to_char(appointment_time+(duration||' mins')::interval,'hh24:mi') AS "
      + "text_end_appointment_only_time, ap.duration,ap.appointment_status,ap.arrival_time, "
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
      + " CASE WHEN apit.resource_type ='EQID' THEN (select equipment_name from"
      + " test_equipment_master where eq_id::text=apit.resource_id )"
      + " WHEN apit.resource_type ='DOC' THEN (select doctor_name from doctors "
      + "where doctor_id=apit.resource_id )"
      + " WHEN apit.resource_type ='DIA' THEN (select test_name from diagnostics "
      + "where test_id::text=apit.resource_id ) "
      + " ELSE (SELECT generic_resource_name FROM generic_resource_type grt "
      + "          JOIN generic_resource_master grm ON(grm.generic_resource_type_id = "
      + "grt.generic_resource_type_id)"
      + "          AND grm.generic_resource_id::text = apit.resource_id "
      + "      WHERE ap.appointment_id = apit.appointment_id AND grt.scheduler_resource_type"
      + " = apit.resource_type)"
      + " END AS resourcename, consultation_type_id  "
      + " FROM scheduler_appointments ap "
      + " LEFT JOIN scheduler_appointment_items apit USING(appointment_id) "
      + " LEFT JOIN doctors pdoc ON (ap.presc_doc_id = pdoc.doctor_id) "
      + " JOIN ( #MASTER_DATA# ) AS r ON (r.resource_id = ap.res_sch_name) "
      + " LEFT JOIN appointment_source_master cas ON "
      + "(cas.appointment_source_id = ap.app_source_id) "
      + " LEFT JOIN  scheduler_master  sm ON sm.res_sch_id = ap.res_sch_id "
      + " LEFT JOIN contact_preferences cf on (ap.mr_no = cf.mr_no) "
      + " LEFT JOIN scheduler_resource_types srt ON (srt.category = sm.res_sch_category "
      + "AND apit.resource_type = srt.resource_type) "
      + " LEFT JOIN tpa_master tpa ON (tpa.tpa_id = ap.primary_sponsor_id) ";

  @Override
  public String getCategory() {
    return CATEGORY;
  }

  @Override
  public String getSecondaryResourceMasterQuery() {
    return SECONDARY_REOSURCE_MASTER_QUERY;
  }

  @Override
  public String getPrimaryResourceMasterQuery() {
    return PRIMARY_REOSURCE_MASTER_QUERY;
  }

  @Override
  public String getDeptFilterClause() {
    return "AND tem.ddept_id IN (:deptIds)";
  }

  @Override
  public void setAppointmentData(List<Appointment> appointmentsList, Map<String, Object> params) {
    Map<String, Object> appontmentInfo = (Map<String, Object>) params.get("appointment");
    String primResId = appontmentInfo.get("primary_resource_id") != null ? (String) appontmentInfo
        .get("primary_resource_id") : null;
    String schResName = appontmentInfo.get("secondary_resource_id") != null ? String
        .valueOf(appontmentInfo.get("secondary_resource_id")) : null;
    for (Appointment appt : appointmentsList) {
      appt.setConsultationTypeId(0);
      appt.setScheduleName(schResName);
      appt.setPrim_res_id(primResId);
    }
  }

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
    AppointmentResource res = new AppointmentResource(appointment.getAppointmentId(), "EQID",
        appointment.getPrim_res_id());
    res.setAppointment_item_id(appointmentItemId);
    res.setUser_name(userName);
    res.setMod_time(modTime);
    appointmentItemsList.add(res);
  }

  @Override
  public String getSecondaryResourceType() {
    return SECONDARY_RESOURCE_TYPE;
  }

  @Override
  public String getPrimaryResourceType() {
    return PRIMARY_RESOURCE_TYPE;
  }

  @Override
  public Integer getResourceOverbookLimit(String resourceId, String resType) {
    switch (resType) {
      case "LABTECH":
      case "DOC":
        return doctorService.getDoctorOverbookLimit(resourceId);
      case "EQID":
        return testEquipmentService.getTestEquipmentOverbookLimit(resourceId);
      default:
        return doctorService.getDoctorOverbookLimit(resourceId);
    }
  }

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

  @Override
  public String getAppointmentDetailsQuery() {
    String query = GET_APPOINTMENT_DETAILS;
    query = query.replace("#MASTER_DATA#", PRIMARY_REOSURCE_MASTER_QUERY);
    return query;
  }

  @Override
  public String getResourceName(String resourceId, String resType) {
    switch (resType) {
      case "DOC":
      case "LABTECH":
        Map<String, Object> doctor = doctorService.getDoctorDetails(resourceId);
        if (doctor != null) {
          return (String) doctor.get("doctor_name");
        }
        return null;
      case "EQID":
        return testEquipmentService.getTestEquipmentName(resourceId);
      default:
        return additionalResourcesQueryProvider.getGenericResourceName(resourceId);
    }
  }


  @Override
  public String validatePrimRes() {
    return "select eq_id,center_id from test_equipment_master where eq_id =  ?::Integer "
        + "AND status = 'A'";
  }

  @Override
  public String validateSecRes() {
    return "select test_id from diagnostics where test_id =  ? AND status = 'A'";
  }

  public static final String GET_DURATION_FROM_SEC_RES = "SELECT test_duration as duration"
      + " from diagnostics where test_id = ?";

  @Override
  public int getAppointmentDuration(String testId, String eqId) {
    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(GET_DURATION_FROM_SEC_RES,
        new Object[] { testId });
    int duration = 0;
    if (bean != null) {
      duration = (int) bean.get("duration");
    } else {
      if (testId != null) {
        duration = getSlotDurationOfPrimRes(testId);
      } else {
        duration = -1;
      }
    }
    return duration;
  }

  public static final String GET_DURATION_FROM_PRIM_RES = "select default_duration as duration"
      + " from scheduler_master where res_sch_name = ? AND res_sch_type = ? AND status = 'A'";

  @Override
  public int getSlotDurationOfPrimRes(String eqId) {
    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(GET_DURATION_FROM_PRIM_RES, new Object[] {
        eqId, "EQID" });
    int duration = 0;
    if (bean != null) {
      duration = (int) bean.get("duration");
    } else {
      bean = DatabaseHelper
          .queryToDynaBean(GET_DURATION_FROM_PRIM_RES, new Object[] { "*", "EQID" });
      duration = (int) bean.get("duration");
    }
    return duration;
  }

  public static final String GET_PRIM_RES = "SELECT distinct tem.eq_id::text as resource_id, "
      + "tem.equipment_name as resource_name, dtem.test_id FROM test_equipment_master tem  "
      + "LEFT JOIN diagnostics_test_equipment_master_mapping dtem ON (tem.eq_id = dtem.eq_id) "
      + "LEFT JOIN diagnostics_departments dept ON(dept.ddept_id=tem.ddept_id) "
      + "WHERE tem.schedule = true AND tem.status='A' AND dept.status = 'A' AND "
      + "dept.ddept_id = ? AND tem.center_id IN (0,?) ";

  public static final String GET_PRIM_RES_WITHOUT_DEPT = "SELECT distinct tem.eq_id::text "
          + "as resource_id, tem.equipment_name as resource_name, dtem.test_id "
          + "FROM test_equipment_master tem  "
          + "LEFT JOIN diagnostics_test_equipment_master_mapping dtem ON (tem.eq_id = dtem.eq_id) "
          + "WHERE tem.schedule = true AND tem.status='A' AND tem.center_id IN (0,?) ";

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public List<Map> getPrimResApplicableForSecRes(String secResId, int centerId, String deptId) {
    List<Map> maplist = ConversionUtils.copyListDynaBeansToMap(DatabaseHelper.queryToDynaList(
        GET_PRIM_RES, new Object[] { deptId, centerId }));
    List<Map> filteredBySecRes = new ArrayList<>();
    if (maplist.size() > 0) {
      for (Map m: maplist) {
        if (m.get("test_id") != null && m.get("test_id").equals(secResId)) {
          filteredBySecRes.add(m);
        }
      }
    }
    if (maplist.size() == 0) {
      maplist = ConversionUtils.copyListDynaBeansToMap(DatabaseHelper.queryToDynaList(
          GET_PRIM_RES_WITHOUT_DEPT, new Object[] { centerId }));
      if (maplist.size() > 0) {
        for (Map m: maplist) {
          if (m.get("test_id") != null && m.get("test_id").equals(secResId)) {
            filteredBySecRes.add(m);
          }
        }
      }
    }
    if (filteredBySecRes.size() > 0) {
      maplist = new ArrayList<>();
      maplist.addAll(filteredBySecRes);
    }
    List responseList = new ArrayList();
    Set resourceIds = new HashSet<>();
    for (Map map : maplist) {
      Map tempMap = new HashMap();
      if (!resourceIds.contains(map.get("resource_id"))) {
        tempMap.putAll(map);
        resourceIds.add(map.get("resource_id"));
        responseList.add(tempMap);
      }
    }
    return responseList;
  }

  @Override
  public List<Map> getSecondaryResources() {
    return Collections.EMPTY_LIST;
  }

  @Override
  public List<Map> getSecondaryResources(String primResId) {
    // TODO Auto-generated method stub
    return null;
  }

}

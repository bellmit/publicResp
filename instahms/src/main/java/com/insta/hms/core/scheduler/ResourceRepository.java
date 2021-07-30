package com.insta.hms.core.scheduler;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.resourcescheduler.ResourceBO.AppointMentResource;
import com.insta.hms.resourcescheduler.ResourceBO.Appointments;
import com.insta.hms.resourcescheduler.ResourceDTO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class ResourceRepository.
 */
@Repository
public class ResourceRepository extends GenericRepository {

  /** The Constant GET_DEFAULT_RESOURCE_AVAILABILITIES. */
  private static final String GET_DEFAULT_RESOURCE_AVAILABILITIES = "SELECT * FROM "
      + "scheduler_master sm "
      + " JOIN sch_default_res_availability_details sdra ON (sdra.res_sch_id = sm.res_sch_id) "
      + " WHERE day_of_week = ? AND sm.res_sch_name = ? AND "
      + "res_sch_type = ? AND from_time != to_time "
      + " AND sm.status = 'A' #  @ ORDER BY sdra.from_time";

  /** The Constant GET_DEFAULT_ATTRIBUTES_FOR_RESOURCE. */
  private static final String GET_DEFAULT_ATTRIBUTES_FOR_RESOURCE = "SELECT * FROM "
      + "scheduler_master WHERE res_sch_category = ? AND res_sch_name = ?";

  /** The Constant GET_APPOINTMENTS_FOR_A_RESOURCE. */
  private static final String GET_APPOINTMENTS_FOR_A_RESOURCE = "SELECT * FROM "
      + " (SELECT sa.*, sai.resource_id, appointment_time + ( duration || ' mins')::interval "
      + "AS end_appointment_time #PD_F# "
      + " FROM scheduler_appointments sa "
      + " JOIN scheduler_appointment_items sai ON (sa.appointment_id = sai.appointment_id) "
      + "#RES# #PD# ) as foo"
      + " WHERE resource_id::text = ? @  AND appointment_status NOT IN ('Cancel','Noshow') "
      + " AND ( appointment_time >= ? AND appointment_time < ? )  "
      + " ORDER BY end_appointment_time ";

  /** The Constant CENTER_ID_WHERE_CLAUSE. */
  private static final String CENTER_ID_WHERE_CLAUSE = " AND center_id = ?  ";

  /** The Constant AVAILABILITY_STATUS_WHERE_CLAUSE. */
  private static final String AVAILABILITY_STATUS_WHERE_CLAUSE = " AND availability_status = ? ";

  /** The Constant PATIENT_DETAILS_FIELDS. */
  private static final String PATIENT_DETAILS_FIELDS = " , pd.patient_phone, pd.email_id ";

  /** The Constant PATIENT_DETAILS_JOIN. */
  private static final String PATIENT_DETAILS_JOIN = " LEFT JOIN patient_details pd ON "
      + "(pd.mr_no = sa.mr_no) ";

  /** The Constant DOCTOR_JOIN_RESOURCE. */
  private static final String DOCTOR_JOIN_RESOURCE = " JOIN doctors res ON"
      + " (res.doctor_id = sai.resource_id) ";

  /** The Constant EQUIPMENT_JOIN_RESOURCE. */
  private static final String EQUIPMENT_JOIN_RESOURCE = " JOIN test_equipment_master res "
      + " ON(res.eq_id::text = sai.resource_id) ";

  /** The Constant SERVICE_JOIN_RESOURCE. */
  private static final String SERVICE_JOIN_RESOURCE = " JOIN service_resource_master res "
      + " ON(res.serv_res_id::text = sai.resource_id) ";

  /** The Constant THEATRE_JOIN_RESOURCE. */
  private static final String THEATRE_JOIN_RESOURCE = " JOIN theatre_master res "
      + " ON(res.theatre_id = sai.resource_id) ";

  /** The Constant GET_NEXT_SEQUENCE. */
  private static final String GET_NEXT_SEQUENCE = "SELECT nextval(?)";

  /** The Constant NEXT_APPOINTMENT_ID_SEQUENCE. */
  private static final String NEXT_APPOINTMENT_ID_SEQUENCE = "scheduler_appointments_seq";

  /** The Constant NEXT_UNIQUE_APPOINTMENT_ID. */
  private static final String NEXT_UNIQUE_APPOINTMENT_ID = "unique_appt_ind";

  /** The Constant NEXT_APPOINTMENT_ITEM_ID. */
  private static final String NEXT_APPOINTMENT_ITEM_ID = "scheduler_appointment_items";

  /** The Constant GET_RESOURCE_OVERRIDES. */
  private static final String GET_RESOURCE_OVERRIDES = "SELECT * FROM "
      + " sch_resource_availability sra "
      + " JOIN sch_resource_availability_details srad ON (srad.res_avail_id = sra.res_avail_id) "
      + " WHERE  res_sch_type = ? AND res_sch_name = ? AND #AVAIL_START# #AVAIL_END# "
      + "from_time != to_time #  @ order by srad.from_time";

  /** The Constant INSERT_APPOINTMENT. */
  private static final String INSERT_APPOINTMENT = "INSERT INTO scheduler_appointments "
      + " (mr_no, patient_name, "
      + " patient_contact, complaint, appointment_id, res_sch_id, res_sch_name, appointment_time,"
      + " duration, appointment_status, booked_by, booked_time, cancel_reason, visit_id, "
      + " consultation_type_id, remarks, changed_by,scheduler_visit_type, scheduler_prior_auth_no,"
      + " scheduler_prior_auth_mode_id, center_id, presc_doc_id, "
      + " salutation_name, unique_appt_ind, prim_res_id, patient_contact_country_code,"
      + " practo_appointment_id, app_source_id) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
      + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  /** The Constant INSERT_APPOINTMENT_ITEMS. */
  private static final String INSERT_APPOINTMENT_ITEMS = "INSERT INTO scheduler_appointment_items"
      + " (appointment_id, resource_type,"
      + " resource_id, appointment_item_id,user_name,mod_time) values (?, ?, ?, ?, ?, ?)";

  /** The Constant GET_APPOINTMENT_SOURCE. */
  private static final String GET_APPOINTMENT_SOURCE = "SELECT * FROM appointment_source_master"
      + "  WHERE appointment_source_name = ?";

  /** The Constant GET_TODAYS_APPOINTMENTS_FOR_MRNO. */
  private static final String GET_TODAYS_APPOINTMENTS_FOR_MRNO = "SELECT sa.appointment_id, "
      + "dc.doctor_name, to_char(sa.appointment_time,'hh24:mi') AS appointment_time "
      + " FROM scheduler_appointments sa "
      + " LEFT JOIN doctors dc  ON dc.doctor_id = sa.prim_res_id "
      + " LEFT JOIN department dcd ON(dc.dept_id=dcd.dept_id) "
      + " WHERE sa.mr_no = ? AND sa.appointment_time::date = current_date";

  /** The Constant GET_APPOINTMENT_DETAILS. */
  private static final String GET_APPOINTMENT_DETAILS = " SELECT ap.mr_no,ap.visit_id,"
      + " ap.patient_contact,ap.complaint,ap.presc_doc_id, ap.contact_id, "
      + " srt.category,ap.remarks,ap.scheduler_visit_type,ap.vip_status,ap.prim_res_id, "
      + " (select doctor_name from doctors where doctor_id=ap.presc_doc_id) as presc_doctor, "
      + " ap.cond_doc_id, (select doctor_name from doctors where doctor_id=ap.cond_doc_id) "
      + " as cond_doctor, "
      + " case when srt.category = 'DOC' then dc.dept_id end as dept_id,ap.center_id, "
      + " ap.scheduler_prior_auth_no,ap.scheduler_prior_auth_mode_id,"
      + " case when srt.category='DIA' AND dd.category = 'DEP_LAB' then 'Laboratory' "
      + " when srt.category='DIA' AND dd.category='DEP_RAD' then 'Radiology' "
      + " when srt.category = 'SNP' then 'Service' end as item_type,"
      + " ap.appointment_id,ap.res_sch_id,ap.res_sch_name,"
      + " CONCAT(COALESCE(package_id::text,''),COALESCE(appointment_pack_group_id::text,'')) "
      + " as order_set_key, "
      + " to_char(ap.appointment_time,'dd-MM-yyyy') AS text_appointment_date,"
      + " to_char(ap.appointment_time,'dd-MM-yyyy hh24:mi:ss') AS text_appointment_date_time,"
      + " to_char(ap.appointment_time,'hh24:mi')::time AS appointment_time,"
      + " to_char(ap.appointment_time,'hh24:mi') AS text_appointment_time,appointment_time "
      + "as appointment_date_time,"
      + " to_char(appointment_time+(duration||' mins')::interval,'dd-MM-yyyy hh24:mi:ss') "
      + "AS text_end_appointment_time,"
      + " to_char(appointment_time+(duration||' mins')::interval,'hh24:mi') "
      + "AS text_end_appointment_only_time,"
      + " ap.duration,ap.appointment_status,ap.arrival_time,ap.completed_time,ap.pat_package_id, "
      + " ap.booked_by,ap.booked_time, b.payment_status, cas.paid_at_source, ap.changed_by,"
      + "ap.changed_time,apit.resource_type,"
      + " apit.resource_id,apit.appointment_item_id,srt.primary_resource,"
      + " CASE WHEN apit.resource_type ='SUDOC' THEN (select doctor_name from "
      + " doctors where doctor_id=apit.resource_id ) "
      + " WHEN apit.resource_type ='ANEDOC' THEN (select doctor_name from doctors "
      + " where doctor_id=apit.resource_id ) "
      + " WHEN apit.resource_type ='EQID' THEN (select equipment_name from test_equipment_master "
      + " where eq_id::text=apit.resource_id ) "
      + " WHEN apit.resource_type ='SRID' THEN (select serv_resource_name from "
      + " service_resource_master where serv_res_id::text=apit.resource_id )"
      + " WHEN apit.resource_type ='THID' THEN (select theatre_name from theatre_master "
      + " where theatre_id=apit.resource_id )"
      + " WHEN apit.resource_type ='OPDOC' THEN (select doctor_name from doctors "
      + " where doctor_id=apit.resource_id )"
      + " WHEN apit.resource_type ='DOC' THEN (select doctor_name from doctors "
      + " where doctor_id=apit.resource_id )"
      + " WHEN apit.resource_type ='LABTECH' THEN (select doctor_name from doctors "
      + " where doctor_id=apit.resource_id ) "
      + " ELSE (SELECT generic_resource_name FROM generic_resource_type grt "
      + " JOIN generic_resource_master grm ON"
      + "(grm.generic_resource_type_id = grt.generic_resource_type_id)"
      + " AND grm.generic_resource_id::text = apit.resource_id "
      + "WHERE ap.appointment_id = apit.appointment_id AND "
      + "grt.scheduler_resource_type = apit.resource_type)"
      + " END AS resourcename, dcd.dept_name as doctor_department,"
      + " COALESCE(dc.doctor_name,dg.test_name,op.operation_name,s.service_name) "
      + "AS central_resource_name,consultation_type_id, "
      + " COALESCE(dg.mandate_additional_info, 'N') as mandate_additional_info,  "
      + "COALESCE(dg.additional_info_reqts, '') as additional_info_reqts,"
      + " cd.salutation_name, cd.patient_dob, cd.patient_age, cd.patient_age_units, "
      + " cd.patient_gender, cd.patient_email_id, coalesce(cd.patient_name, ap.patient_name)"
      + " as patient_name, cd.last_name, ppp.patient_presc_id, pp.consultation_id, "
      + " COALESCE(ppprd.doctor_name, ppprr.referal_name) AS ppd_osp_referral_doctor,"
      + " pppr.reference_docto_id as ppd_osp_referral_doctor_id, "
      + " ppp.pat_pending_presc_id, ppp.preauth_activity_id,  "
      + " ppa.preauth_act_type, ppa.preauth_act_item_id, ppa.preauth_id, "
      + " ppa.preauth_mode, ppa.preauth_required"
      + " FROM scheduler_appointments ap "
      + " LEFT JOIN contact_details cd ON (cd.contact_id = ap.contact_id)"
      + " LEFT JOIN scheduler_appointment_items apit USING(appointment_id) "
      + " LEFT JOIN doctors dc  ON dc.doctor_id = ap.prim_res_id "
      + " LEFT JOIN department dcd ON(dc.dept_id=dcd.dept_id)"
      + " LEFT JOIN diagnostics dg ON dg.test_id = ap.res_sch_name "
      + " LEFT JOIN services s ON s.service_id = ap.res_sch_name "
      + " LEFT JOIN operation_master op ON op.op_id = ap.res_sch_name "
      + " LEFT JOIN diagnostics_departments dd ON dg.ddept_id = dd.ddept_id"
      + " LEFT JOIN bill b ON (ap.bill_no = b.bill_no) "
      + " LEFT JOIN appointment_source_master cas ON "
      + "(cas.appointment_source_id = ap.app_source_id) "
      + " LEFT JOIN  scheduler_master  sm ON sm.res_sch_id = ap.res_sch_id "
      + " LEFT JOIN scheduler_resource_types srt ON (srt.category = sm.res_sch_category AND "
      + "apit.resource_type = srt.resource_type)"
      + " LEFT JOIN patient_pending_prescriptions ppp "
      + " ON (ppp.pat_pending_presc_id = ap.patient_presc_id) "
      + " LEFT JOIN patient_prescription pp on (ppp.patient_presc_id = pp.patient_presc_id)"
      + " LEFT JOIN patient_registration pppr on "
      + "   (ppp.visit_id = pppr.patient_id and pppr.visit_type='o' and pppr.op_type='O')"
      + " LEFT JOIN doctors ppprd ON pppr.reference_docto_id = ppprd.doctor_id"
      + " LEFT JOIN referral ppprr ON pppr.reference_docto_id = ppprr.referal_no"
      + " LEFT JOIN preauth_prescription_activities ppa "
      + " ON (ppp.preauth_activity_id = ppa.preauth_act_id)";

  /** The Constant GET_APPOINTMENT_DETAILS_WHERE_CLAUSE. */
  private static final String GET_APPOINTMENT_DETAILS_WHERE_CLAUSE = " WHERE appointment_id  = ?";

  /** The Constant GET_APPOINTMENT_DETAILS_WHERE_PACK_GRP. */
  private static final String GET_APPOINTMENT_DETAILS_WHERE_PACK_GRP = " WHERE "
      + " appointment_pack_group_id  = ?";

  /** The Constant INSERT_RESOURCE. */
  private static final String INSERT_RESOURCE = "INSERT INTO scheduler_appointment_items"
      + "(appointment_id, resource_type, resource_id, user_name, mod_time) "
      + " values (?,?,?,?,?)";

  /** The Constant UPDATE_RESOURCE. */
  private static final String UPDATE_RESOURCE = "UPDATE scheduler_appointment_items SET "
      + "resource_id=?,user_name=?, mod_time = ? WHERE "
      + " appointment_id=? and resource_type=? and appointment_item_id=?";

  /** The Constant DELETE_RESOURCE. */
  private static final String DELETE_RESOURCE = "DELETE FROM scheduler_appointment_items WHERE "
      + " appointment_id=?  and resource_id=? and resource_type=?";

  /** The Constant GET_APPOINTMENT_ITEMS. */
  private static final String GET_APPOINTMENT_ITEMS = " SELECT * FROM scheduler_appointment_items"
      + " WHERE appointment_id = ?";

  /**
   * Instantiates a new resource repository.
   */
  public ResourceRepository() {
    super("scheduler_appointments");
  }

  /** The Constant GET_APPOINTMENT_STATUS_FROM_CONSULTATION. */
  public static final String GET_APPOINTMENT_STATUS_FROM_CONSULTATION = "SELECT status FROM "
      + "doctor_consultation "
      + " WHERE appointment_id = ?";

  /** The Constant GET_APPOINTMENT_STATUS_FROM_DIAGNOSTIC. */
  public static final String GET_APPOINTMENT_STATUS_FROM_DIAGNOSTIC = "SELECT conducted as"
      + " status FROM tests_prescribed  "
      + " WHERE appointment_id = ?";

  /** The Constant GET_APPOINTMENT_STATUS_FROM_OPERATION. */
  public static final String GET_APPOINTMENT_STATUS_FROM_OPERATION = "SELECT status "
      + " FROM bed_operation_schedule " + " WHERE appointment_id = ? ";

  /** The Constant GET_APPOINTMENT_STATUS_FROM_SERVICES. */
  public static final String GET_APPOINTMENT_STATUS_FROM_SERVICES = "SELECT conducted as status"
      + " FROM services_prescribed sp "
      + " WHERE appointment_id = ?";

  /** The Constant GET_APPOINTMENT_SOURCE_WITH_APPOINTMENT_ID. */
  public static final String GET_APPOINTMENT_SOURCE_WITH_APPOINTMENT_ID = " select "
      + "casm.appointment_source_name from appointment_source_master casm "
      + "join scheduler_appointments sa on(casm.appointment_source_id=sa.app_source_id) "
      + "where appointment_id=? ";

  /**
   * Gets the default attributes of resource.
   *
   * @param category the category
   * @param resourceId the resource id
   * @return the default attributes of resource
   */
  protected BasicDynaBean getDefaultAttributesOfResource(String category, String resourceId) {

    return DatabaseHelper.queryToDynaBean(GET_DEFAULT_ATTRIBUTES_FOR_RESOURCE,
        new Object[] { category, resourceId });
  }

  /**
   * Gets the Weekly Availability of a resource for a given day.
   *
   * @param resourceId
   *          - The unique identifier for resource
   * @param dayOfWeek
   *          - The day of week. 0 represents SUNDAY, 1 represents MONDAY, 2 TUESDAY..., 6
   *          represents SATURDAY
   * @param resourceType
   *          - The type of resource. Eg : Doctors, Operation theaters
   * @param availabilityStatus
   *          - Whether the resource is available. 'A' for Available , 'N' for Not Available
   * @param centerId
   *          - The center for which weekly availability is to be fetched
   * @param centersIncDefault
   *          - Total centers including default center
   * @return the default resource availabilities
   */
  public List<BasicDynaBean> getDefaultResourceAvailabilities(String resourceId, int dayOfWeek,
      String resourceType, String availabilityStatus, Integer centerId, int centersIncDefault) {
    List<Object> queryParams = new ArrayList<Object>();

    String query = GET_DEFAULT_RESOURCE_AVAILABILITIES;
    if (availabilityStatus != null) {
      query = query.replace("#", AVAILABILITY_STATUS_WHERE_CLAUSE);
    } else {
      query = query.replace("#", "");
    }
    if (centerId != null && centersIncDefault > 1) {
      query = query.replace("@", CENTER_ID_WHERE_CLAUSE);
    } else {
      query = query.replace("@", "");
    }
    queryParams.add(dayOfWeek);
    queryParams.add(resourceId);
    queryParams.add(resourceType);
    if (availabilityStatus != null) {
      queryParams.add(availabilityStatus);
    }
    if (centerId != null && centersIncDefault > 1) {
      queryParams.add(centerId);
    }
    return DatabaseHelper.queryToDynaList(query, queryParams.toArray());

  }

  /**
   * Gets All the appointments for a given resource in a given center whose appointments start in
   * the range [startTime, endTime).
   *
   * @param resourceId          - Unique identifier for a resource.
   * @param resourceType          - Type of resource Ex: Doctor, Operation theaters
   * @param centerId          - The center for which appointments are to be fetched
   * @param startTime          - The Lower range for appointments start time
   * @param endTime          - The upper range for the appointments start time
   * @param incPatientDetails          - Whether to fetch patient details for an appointment
   * @return the resource appointments
   */
  protected List<BasicDynaBean> getResourceAppointments(String resourceId, String resourceType,
      Integer centerId, Timestamp startTime, Timestamp endTime, boolean incPatientDetails) {
    String query = GET_APPOINTMENTS_FOR_A_RESOURCE;
    String replaceWith = "";
    if (resourceType.equals("DOC")) {
      replaceWith = DOCTOR_JOIN_RESOURCE;

    } else if (resourceType.equals("OPE")) {
      replaceWith = THEATRE_JOIN_RESOURCE;

    } else if (resourceType.equals("DIA")) {
      replaceWith = EQUIPMENT_JOIN_RESOURCE;

    } else if (resourceType.equals("SNP")) {
      replaceWith = SERVICE_JOIN_RESOURCE;
    }
    query = query.replace("#RES#", replaceWith);

    if (centerId != null) {
      query = query.replace("@", CENTER_ID_WHERE_CLAUSE);
    } else {
      query = query.replace("@", " ");
    }
    if (incPatientDetails) {
      query = query.replace("#PD#", PATIENT_DETAILS_JOIN);
      query = query.replace("#PD_F#", PATIENT_DETAILS_FIELDS);
    } else {
      query = query.replace("#PD#", " ");
      query = query.replace("#PD_F#", " ");
    }
    List<Object> queryParams = new ArrayList<Object>();
    queryParams.add(resourceId);
    if (centerId != null) {
      queryParams.add(centerId);
    }
    queryParams.add(startTime);
    queryParams.add(endTime);

    return DatabaseHelper.queryToDynaList(query, queryParams.toArray());
  }

  /**
   * Get the list of overrides for a doctor in a given day and a given center.
   *
   * @param resourceType
   *          - The type of Resource Eg: Doctors, Operation Theaters
   * @param fromDate
   *          - The date from which overrides are to be fetched (inclusive)
   * @param endDate
   *          - Date until which overrides are to be fetched (inclusive)
   * @param resourceId
   *          - The unique identifier of Resource
   * @param availabilityStatus
   *          - Whether the resource is available. 'A' for Available , 'N' for Not Available
   * @param centerId
   *          - The center for which overrides are to be fetched
   * @param centersIncDefault
   *          - Total number of centers
   * @return the resource overrides
   */
  public List<BasicDynaBean> getResourceOverrides(String resourceType, Date fromDate, Date endDate,
      String resourceId, String availabilityStatus, Integer centerId, Integer centersIncDefault) {

    String query = GET_RESOURCE_OVERRIDES;
    if (fromDate != null) {
      query = query.replace("#AVAIL_START#", "availability_date >= ? AND ");
    } else {
      query = query.replace("#AVAIL_START#", "");
    }
    if (endDate != null) {
      query = query.replace("#AVAIL_END#", "availability_date <= ? AND ");
    } else {
      query = query.replace("#AVAIL_END#", "");
    }
    if (availabilityStatus != null) {
      query = query.replace("#", AVAILABILITY_STATUS_WHERE_CLAUSE);
    } else {
      query = query.replace("#", "");
    }
    if (centerId != null && centersIncDefault > 1) {
      query = query.replace("@", CENTER_ID_WHERE_CLAUSE);
    } else {
      query = query.replace("@", "");
    }
    List<Object> queryParams = new ArrayList<Object>();
    queryParams.add(resourceType);
    queryParams.add(resourceId);
    if (fromDate != null) {
      queryParams.add(fromDate);
    }
    if (endDate != null) {
      queryParams.add(endDate);
    }

    if (availabilityStatus != null) {
      queryParams.add(availabilityStatus);
    }
    if (centerId != null && centersIncDefault > 1) {
      queryParams.add(centerId);
    }
    return DatabaseHelper.queryToDynaList(query, queryParams.toArray());

  }

  /**
   * Gets the appointment source with appointment id.
   *
   * @param appointmentId the appointment id
   * @return the appointment source with appointment id
   */
  public String getAppointmentSourceWithAppointmentId(Integer appointmentId) {
    BasicDynaBean result = DatabaseHelper.queryToDynaBean(
        GET_APPOINTMENT_SOURCE_WITH_APPOINTMENT_ID, new Object[] { appointmentId });
    if (result != null) {
      return (String) result.get("appointment_source_name");
    } else {
      return null;
    }
  }

  /**
   * Insert appointments.
   *
   * @param appointments the appointments
   * @return true, if successful
   */
  protected boolean insertAppointments(List<Appointments> appointments) {
    List<Object[]> queryParamsList = new ArrayList<Object[]>();
    boolean success = true;
    String query = INSERT_APPOINTMENT;
    for (Appointments appointment : appointments) {
      List<Object> queryParams = new ArrayList<Object>();
      queryParams.add(appointment.getMrNo());
      queryParams.add(appointment.getPatientName());
      queryParams.add(appointment.getPhoneNo());
      queryParams.add(appointment.getComplaint());
      queryParams.add(appointment.getAppointmentId());
      queryParams.add(appointment.getScheduleId());
      queryParams.add(appointment.getScheduleName());
      queryParams.add(appointment.getAppointmentTime());
      queryParams.add(appointment.getAppointmentDuration());
      queryParams.add(appointment.getAppointStatus());
      queryParams.add(appointment.getBookedBy());
      queryParams.add(appointment.getBookedTime());
      queryParams.add(appointment.getCancelReason());
      queryParams.add(appointment.getVisitId());
      queryParams.add(appointment.getConsultationTypeId());
      queryParams.add(appointment.getRemarks());
      queryParams.add(appointment.getChangedBy());
      queryParams.add(appointment.getSchedulerVisitType());
      queryParams.add(appointment.getSchPriorAuthId());
      queryParams.add(appointment.getSchPriorAuthModeId());
      queryParams.add(appointment.getCenterId());
      queryParams.add(appointment.getPrescDocId());
      queryParams.add(appointment.getSalutationName());
      queryParams.add(appointment.getUnique_appt_ind());
      queryParams.add(appointment.getPrim_res_id());
      queryParams.add(appointment.getPhoneCountryCode());
      queryParams.add(appointment.getPractoAppointmentId());
      queryParams.add(appointment.getApp_source_id());
      queryParamsList.add(queryParams.toArray());
    }
    int[] results = DatabaseHelper.batchInsert(query, queryParamsList);

    for (int result : results) {
      if (result < 0) {
        success = false;
        break;
      }
    }
    return success;
  }

  /**
   * Insert appointment items.
   *
   * @param list the list
   * @return true, if successful
   */
  protected boolean insertAppointmentItems(List<AppointMentResource> list) {
    List<Object[]> queryParamsList = new ArrayList<Object[]>();
    String query = INSERT_APPOINTMENT_ITEMS;
    boolean success = true;
    for (AppointMentResource appointmentResource : list) {
      List<Object> queryParams = new ArrayList<Object>();
      queryParams.add(appointmentResource.getAppointmentId());
      queryParams.add(appointmentResource.getResourceType());
      queryParams.add(appointmentResource.getResourceId());
      queryParams.add(appointmentResource.getAppointment_item_id());
      queryParams.add(appointmentResource.getUser_name());
      queryParams.add(appointmentResource.getMod_time());
      queryParamsList.add(queryParams.toArray());
    }
    int[] results = DatabaseHelper.batchInsert(query, queryParamsList);

    for (int result : results) {
      if (result < 0) {
        success = false;
        break;
      }
    }
    return success;
  }

  /**
   * Update resources.
   *
   * @param resourceUpdateList the resource update list
   * @return true, if successful
   */
  protected boolean updateResources(List<ResourceDTO> resourceUpdateList) {
    List<Object[]> queryParamsList = new ArrayList<Object[]>();
    String query = UPDATE_RESOURCE;
    boolean success = true;
    for (ResourceDTO resourceDTO : resourceUpdateList) {
      List<Object> queryParams = new ArrayList<Object>();
      queryParams.add(resourceDTO.getResourceId());
      queryParams.add(resourceDTO.getUser_name());
      queryParams.add(resourceDTO.getMod_time());
      queryParams.add(resourceDTO.getAppointmentId());
      queryParams.add(resourceDTO.getResourceType());
      queryParams.add(resourceDTO.getAppointment_item_id());
      queryParamsList.add(queryParams.toArray());
    }
    int[] results = DatabaseHelper.batchUpdate(query, queryParamsList);

    for (int result : results) {
      if (result < 0) {
        success = false;
        break;
      }
    }
    return success;
  }

  /**
   * Insert resources.
   *
   * @param resourceInsertList the resource insert list
   * @return true, if successful
   */
  protected boolean insertResources(List<ResourceDTO> resourceInsertList) {
    List<Object[]> queryParamsList = new ArrayList<Object[]>();
    String query = INSERT_RESOURCE;
    boolean success = true;
    for (ResourceDTO resourceDTO : resourceInsertList) {
      List<Object> queryParams = new ArrayList<Object>();
      queryParams.add(resourceDTO.getAppointmentId());
      queryParams.add(resourceDTO.getResourceType());
      queryParams.add(resourceDTO.getResourceId());
      queryParams.add(resourceDTO.getUser_name());
      queryParams.add(resourceDTO.getMod_time());
      queryParamsList.add(queryParams.toArray());

    }
    int[] results = DatabaseHelper.batchInsert(query, queryParamsList);

    for (int result : results) {
      if (result < 0) {
        success = false;
        break;
      }
    }
    return success;
  }

  /**
   * Delete resources.
   *
   * @param resourceDeleteList the resource delete list
   * @return true, if successful
   */
  protected boolean deleteResources(List<ResourceDTO> resourceDeleteList) {
    List<Object[]> queryParamsList = new ArrayList<Object[]>();
    String query = DELETE_RESOURCE;
    boolean success = true;
    for (ResourceDTO resourceDTO : resourceDeleteList) {
      List<Object> queryParams = new ArrayList<Object>();
      queryParams.add(resourceDTO.getAppointmentId());
      queryParams.add(resourceDTO.getResourceId());
      queryParams.add(resourceDTO.getResourceType());
      queryParamsList.add(queryParams.toArray());
    }
    int[] results = DatabaseHelper.batchDelete(query, queryParamsList);

    for (int result : results) {
      if (result < 0) {
        success = false;
        break;
      }
    }
    return success;
  }

  /**
   * Gets the next appointment id.
   *
   * @return the next appointment id
   */
  protected Integer getNextAppointmentId() {
    return DatabaseHelper.getInteger(GET_NEXT_SEQUENCE, NEXT_APPOINTMENT_ID_SEQUENCE);

  }

  /**
   * Gets the next unique appointment.
   *
   * @return the next unique appointment
   */
  protected Integer getNextUniqueAppointment() {
    return DatabaseHelper.getNextSequence(NEXT_UNIQUE_APPOINTMENT_ID);
  }

  /**
   * Gets the next appointment item id.
   *
   * @return the next appointment item id
   */
  public Integer getNextAppointmentItemId() {
    return DatabaseHelper.getNextSequence(NEXT_APPOINTMENT_ITEM_ID);
  }

  /**
   * Gets the appointment source.
   *
   * @param appointmentSource the appointment source
   * @return the appointment source
   */
  public BasicDynaBean getAppointmentSource(String appointmentSource) {
    return DatabaseHelper.queryToDynaBean(GET_APPOINTMENT_SOURCE,
        new Object[] { appointmentSource });
  }

  /**
   * Gets the todays appointments for mrno.
   *
   * @param mrNo the mr no
   * @return the todays appointments for mrno
   */
  public List<BasicDynaBean> getTodaysAppointmentsForMrno(String mrNo) {
    return DatabaseHelper.queryToDynaList(GET_TODAYS_APPOINTMENTS_FOR_MRNO,
        new Object[] { mrNo });
  }

  /**
   * Gets the appointment details.
   *
   * @param appointmentId the appointment id
   * @param appointmentPackGroupId the appointment pack group id
   * @return the appointment details
   */
  public List<BasicDynaBean> getAppointmentDetails(Integer appointmentId, 
      Integer appointmentPackGroupId) {
    if (appointmentPackGroupId != null) {
      return DatabaseHelper.queryToDynaList(
          GET_APPOINTMENT_DETAILS 
          + GET_APPOINTMENT_DETAILS_WHERE_PACK_GRP + " order by appointment_time ",
          new Object[] { appointmentPackGroupId });      
    } else {
      return DatabaseHelper.queryToDynaList(
          GET_APPOINTMENT_DETAILS + GET_APPOINTMENT_DETAILS_WHERE_CLAUSE,
          new Object[] { appointmentId });
    }    
  }

  /** The Constant GET_PATIENTS_ALL_TODAYS_APPOINTMENTS_WHERE_CLAUSE. */
  private static final String GET_PATIENTS_ALL_TODAYS_APPOINTMENTS_WHERE_CLAUSE = " WHERE "
      + "ap.mr_no = ?  AND appointment_status IN(?,?) "
      + "AND date(appointment_time) = ?";
  
  /** The Constant GET_CONTACT_PATIENTS_ALL_TODAYS_APPOINTMENTS_WHERE_CLAUSE. */
  private static final String GET_CONTACT_PATIENTS_ALL_TODAYS_APPOINTMENTS_WHERE_CLAUSE = " WHERE "
      + "ap.contact_id = ?  AND appointment_status IN(?,?) "
      + "AND date(appointment_time) = ?";

  /** The Constant OP_TEST_SERVICE_DOCTOR_APPOINTMENTS_CONDITION. */
  private static final String OP_TEST_SERVICE_DOCTOR_APPOINTMENTS_CONDITION = " AND srt.category"
      + " in ('DIA', 'SNP', 'DOC') AND srt.primary_resource = 't' ";

  /**
   * Gets the todays OP appointments.
   *
   * @param mrNo the mr no
   * @param centerId the center id
   * @return the todays OP appointments
   */
  public List<BasicDynaBean> getTodaysOPAppointments(String mrNo, Integer centerId) {
    String query = GET_APPOINTMENT_DETAILS + GET_PATIENTS_ALL_TODAYS_APPOINTMENTS_WHERE_CLAUSE
        + OP_TEST_SERVICE_DOCTOR_APPOINTMENTS_CONDITION;
    if (centerId != null && centerId != 0) {
      query = query + " AND ap.center_id = '" + centerId + "' ";
    }
    query = query + " order by appointment_time";
    return DatabaseHelper.queryToDynaList(query,
        new Object[] { mrNo, "Booked", "Confirmed", DateUtil.getCurrentDate() });
  }
  
  /**
   * Gets the todays OP appointments.
   *
   * @param contactId the contact id
   * @param centerId the center id
   * @return the todays OP appointments
   */
  public List<BasicDynaBean> getTodaysOPAppointments(Integer contactId, Integer centerId) {
    String query = GET_APPOINTMENT_DETAILS
        + GET_CONTACT_PATIENTS_ALL_TODAYS_APPOINTMENTS_WHERE_CLAUSE
        + OP_TEST_SERVICE_DOCTOR_APPOINTMENTS_CONDITION;
    query = (centerId != null && centerId != 0)
        ? query + " AND ap.center_id = '" + centerId + "' " + " order by appointment_time"
        : query + " order by appointment_time";
    return DatabaseHelper.queryToDynaList(query,
        new Object[] { contactId, "Booked", "Confirmed", DateUtil.getCurrentDate() });
  }

  /**
   * Gets the appointment items.
   *
   * @param appointmentId the appointment id
   * @return the appointment items
   */
  public List<BasicDynaBean> getAppointmentItems(int appointmentId) {
    return DatabaseHelper.queryToDynaList(GET_APPOINTMENT_ITEMS, new Object[] { appointmentId });
  }

  /** The Constant GET_CONDUCTION_FOR_TEST. */
  private static final String GET_CONDUCTION_FOR_TEST = "SELECT conduction_applicable "
      + "FROM diagnostics WHERE test_id = ?";

  /** The Constant GET_CONDUCTION_FOR_SERVICE. */
  private static final String GET_CONDUCTION_FOR_SERVICE = "SELECT conduction_applicable "
      + "FROM services WHERE service_id = ?";

  /** The Constant GET_CONDUCTION_FOR_SURGERY. */
  private static final String GET_CONDUCTION_FOR_SURGERY = "SELECT conduction_applicable "
      + "FROM operation_master WHERE op_id = ?";

  /**
   * Gets the test conduction.
   *
   * @param scheduleId the schedule id
   * @return the test conduction
   */
  public BasicDynaBean getTestConduction(String scheduleId) {
    return DatabaseHelper.queryToDynaBean(GET_CONDUCTION_FOR_TEST, new Object[] { scheduleId });
  }

  /**
   * Gets the operation conduction.
   *
   * @param scheduleId the schedule id
   * @return the operation conduction
   */
  public BasicDynaBean getOperationConduction(String scheduleId) {
    return DatabaseHelper.queryToDynaBean(GET_CONDUCTION_FOR_SURGERY, new Object[] { scheduleId });
  }

  /**
   * Gets the service conduction.
   *
   * @param scheduleId the schedule id
   * @return the service conduction
   */
  public BasicDynaBean getServiceConduction(String scheduleId) {
    return DatabaseHelper.queryToDynaBean(GET_CONDUCTION_FOR_SERVICE, new Object[] { scheduleId });
  }

  /**
   * Checks if is appointment completed.
   *
   * @param appointmentId the appointment id
   * @param category the category
   * @return true, if is appointment completed
   */
  public boolean isAppointmentCompleted(int appointmentId, String category) {
    BasicDynaBean bean = null;
    if (category.equals("DOC")) {
      bean = DatabaseHelper.queryToDynaBean(GET_APPOINTMENT_STATUS_FROM_CONSULTATION,
          new Object[] { appointmentId });
    } else if (category.equals("SNP")) {
      bean = DatabaseHelper.queryToDynaBean(GET_APPOINTMENT_STATUS_FROM_SERVICES,
          new Object[] { appointmentId });
    } else if (category.equals("DIA")) {
      bean = DatabaseHelper.queryToDynaBean(GET_APPOINTMENT_STATUS_FROM_DIAGNOSTIC,
          new Object[] { appointmentId });
    } else if (category.equals("OPE")) {
      bean = DatabaseHelper.queryToDynaBean(GET_APPOINTMENT_STATUS_FROM_OPERATION,
          new Object[] { appointmentId });
    }
    return bean != null && bean.get("status") != null && bean.get("status").equals("C");
  }
  
  /** The Constant FLUSH_CONTACT_FROM_SCHEDULER. */
  private static final String FLUSH_CONTACT_FROM_SCHEDULER = 
      "Update scheduler_appointments set mr_no = ?, contact_id = null where contact_id = ? ";
  
  /** The Constant DELETE_CONTACT_RECORD. */
  private static final String DELETE_CONTACT_RECORD = 
      "delete from contact_details where contact_id = ?";
  
  /**
   * Flush contact.
   *
   * @param mrNo the mr no
   * @param contactId the contact id
   */
  public void flushContact(String mrNo, Integer contactId) {
    DatabaseHelper.update(FLUSH_CONTACT_FROM_SCHEDULER, new Object[] { mrNo,contactId });
    DatabaseHelper.update(DELETE_CONTACT_RECORD, new Object[] { contactId });
  }

}

package com.insta.hms.core.scheduler;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.integration.sms.InstaSmsUpdatedAppointmentLogRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class AppointmentRepository.
 */
@Repository
public class AppointmentRepository extends GenericRepository {

  /** The appointment items repository. */
  @LazyAutowired
  private AppointmentItemsRepository appointmentItemsRepository;

  /** Insta sms update appointment log repo. */
  @LazyAutowired
  private InstaSmsUpdatedAppointmentLogRepository instaSmsUpdatedAppointmentLogRepository;
  
  /**
   * Instantiates a new appointment repository.
   */
  public AppointmentRepository() {
    super("scheduler_appointments");
  }

  /** The Constant GET_APPOINTMENTS. */
  private static final String GET_APPOINTMENTS = "SELECT "
      + " CASE WHEN (uca.confidentiality_grp_id IS NULL AND uma.id IS NULL "
      + " AND pd.patient_group !=0) THEN "
      + "'Xxxxxxxxxxxxxxxxx' ELSE sa.mr_no END AS mr_no, "
      + " CASE WHEN (uca.confidentiality_grp_id IS NULL AND uma.id IS NULL "
      + " AND pd.patient_group !=0) THEN "
      + "'Xxxxxxxxxxxxxxxxx' ELSE sa.visit_id END AS visit_id, "
      + " CASE WHEN (uca.confidentiality_grp_id IS NULL AND uma.id IS NULL "
      + " AND pd.patient_group !=0) THEN "
      + "'Xxxxxxxxxxxxxxxxx' ELSE sa.patient_name END AS patient_name, "
      + " CASE WHEN (uca.confidentiality_grp_id IS NULL AND uma.id IS NULL "
      + " AND pd.patient_group !=0) THEN "
      + "'Xxxxxxxxxxxxxxxxx' ELSE sa.patient_contact END AS patient_contact, "
      + " CASE WHEN (uca.confidentiality_grp_id IS NULL AND uma.id IS NULL "
      + " AND pd.patient_group !=0) THEN "
      + "'N' ELSE 'Y' END AS is_patient_group_accessible, "
      + "   sa.appointment_id, sa.appointment_time, sa.res_sch_name, sa.prim_res_id,"
      + "   sa.center_id, hcm.center_code, hcm.center_name, sa.duration, sa.package_id,"
      + "   sa.appointment_status, sa.booked_by, sa.booked_time, sa.changed_by,"
      + " sa.cancel_type, sa.cancel_reason, sa.visit_mode,"
      + "coalesce(pd.patient_gender,cont.patient_gender) as patient_gender, pd.patient_group, "
      + " CASE WHEN cpref.receive_communication in ('S','B') then 'Y' else 'N' end as send_sms, "
      + " CASE WHEN cpref.receive_communication in ('E','B') then 'Y' else 'N' end as send_email, "
      + " cpref.lang_code, "
      + " coalesce(get_patient_age(pd.dateofbirth,pd.expected_dob),cont.patient_age) as age ,"
      + " CASE WHEN coalesce(pd.patient_gender,cont.patient_gender) = 'M' THEN 'Male'"
      + " WHEN coalesce(pd.patient_gender,cont.patient_gender) = 'F' THEN 'Female'"
      + " WHEN coalesce(pd.patient_gender,cont.patient_gender) = 'O' THEN 'Others' "
      + " WHEN coalesce(pd.patient_gender,cont.patient_gender) = 'C' THEN 'Couple' "
      + " WHEN coalesce(pd.patient_gender,cont.patient_gender) = 'N'  THEN 'Not Applicable' "
      + " ELSE coalesce(pd.patient_gender,cont.patient_gender) END AS patient_gender_text,"
      + " coalesce(cont.patient_age_units,get_patient_age_in(pd.dateofbirth, pd.expected_dob)"
      + " ) as age_in, sa.waitlist, "
      + "   sa.changed_time, sa.scheduler_visit_type AS appointment_visit_type,sai.resource_type, "
      + "sai.resource_id, sm.res_sch_category, "
      + "   sm.res_sch_type, sa.complaint, sa.remarks, sa.presc_doc_id, sa.vip_status, CASE "
      + " WHEN res_sch_category='OPE' then "
      + "(select operation_name from operation_master where op_id=sa.res_sch_name) "
      + " WHEN res_sch_category='DIA' then "
      + "(select test_name from diagnostics where test_id=sa.res_sch_name) "
      + " WHEN res_sch_category='SNP' then "
      + "(select service_name from services where service_id=sa.res_sch_name) "
      + " ELSE '' END as appointment_reason_detail , "
      + "   (SELECT doctor_name FROM doctors WHERE doctor_id=sa.presc_doc_id) AS presc_doctor,"
      + "   #MASTER_DATA_FIELDS# asm.appointment_source_name,"
      + "       cgm.abbreviation, pd.patient_group "
      // + " r.resource_name,r.dept_name,asm.appointment_source_name "
      + "FROM scheduler_appointments sa " + "JOIN scheduler_master sm USING(res_sch_id) "
      + " LEFT JOIN contact_preferences cpref on (sa.mr_no = cpref.mr_no) "
      + "JOIN scheduler_appointment_items sai USING(appointment_id) " + " #MASTER_DATA# "
      // + " JOIN ( #MASTER_DATA# ) AS r ON (r.resource_id = sai.resource_id)"
      + "JOIN hospital_center_master hcm ON(hcm.center_id=sa.center_id) "
      + "LEFT JOIN appointment_source_master asm ON (asm.appointment_source_id = sa.app_source_id) "
      + "LEFT JOIN contact_details cont ON (cont.contact_id = sa.contact_id ) "
      + " LEFT JOIN patient_details pd ON (pd.mr_no = sa.mr_no) "
      + " LEFT JOIN confidentiality_grp_master cgm ON "
      + " (cgm.confidentiality_grp_id = pd.patient_group" + " AND cgm.confidentiality_grp_id != 0) "
      + " LEFT JOIN user_confidentiality_association uca "
      + " ON (uca.confidentiality_grp_id = pd.patient_group "
      + " AND uca.status = 'A' #EMPUSERNAMEFILTER#) "
      + " LEFT JOIN user_mrno_association uma ON (uma.mr_no = sa.mr_no "
      + " #EMPUSERNAMEFILTERFORMRNO#) ";

  /** The Constant GET_NEXT_UNIQUE_APPT_IND. */
  private static final String GET_NEXT_UNIQUE_APPT_IND = "SELECT nextval('unique_appt_ind_seq')";

  /** The Constant GET_NEXT_UNIQUE_APPOINTMENT_PACKAGE_GROUP_ID. */
  private static final String GET_NEXT_UNIQUE_APPOINTMENT_PACKAGE_GROUP_ID = "SELECT nextval"
      + "('unique_appt_package_grp_id')";

  /** The Constant CHECK_IF_SLOT_IS_BOOKED. */
  private static final String CHECK_IF_SLOT_IS_BOOKED = "SELECT * FROM "
      + " (SELECT *, appointment_time +(duration||' mins')::interval AS end_appointment_time "
      + " FROM scheduler_appointments sa "
      + " JOIN scheduler_appointment_items sai using(appointment_id)) as foo" + " WHERE ("
      + " (appointment_time <= :startApptTime AND end_appointment_time > :startApptTime) "
      + " OR (appointment_time >= :startApptTime AND appointment_time < :endApptTime))"
      + " AND appointment_status NOT IN('Cancel','Noshow')"
      + " AND appointment_id != :appointmentId AND #resource_match# AND "
      + " foo.resource_id = :primResId #waitlist ORDER BY end_appointment_time ";

  /** The Constant CHECK_IF_APPOINTMENT_EXISTS_FOR_PATIENT_. */
  public static final String CHECK_IF_APPOINTMENT_EXISTS_FOR_PATIENT_ = "SELECT * FROM "
      + "(SELECT *,appointment_time+(duration||' mins')::interval AS end_appointment_time "
      + " FROM scheduler_appointments sa )as foo" + " WHERE ( "
      + " (appointment_time <= :startApptTime AND end_appointment_time > :startApptTime) "
      + " OR (appointment_time >= :startApptTime AND appointment_time < :endApptTime)) "
      + " AND appointment_status NOT IN('Cancel','Noshow') "
      + " AND appointment_id != :appointmentId" + " # " + " ORDER BY end_appointment_time ";

  /** The Constant CHECK_IF_APPOINTMENT_EXISTS_FOR_PATIENT_FOR_SAME_PACKAGE. */
  public static final String CHECK_IF_APPOINTMENT_EXISTS_FOR_PATIENT_FOR_SAME_PACKAGE = "SELECT * "
      + " FROM (SELECT *,appointment_time+(duration||' mins')::interval AS end_appointment_time "
      + " FROM scheduler_appointments sa )as foo" + " WHERE ("
      + " (appointment_time <= :startApptTime AND end_appointment_time > :startApptTime) "
      + " OR (appointment_time >= :startApptTime AND appointment_time < :endApptTime))"
      + " AND appointment_status NOT IN('Cancel','Noshow') "
      + " AND appointment_id != :appointmentId"
      + " AND (appointment_pack_group_id != :groupId OR package_id != :packageId) # "
      + " ORDER BY end_appointment_time ";

  /** The Constant GET_APPOINTMENT_DETAILS. */
  private static final String GET_APPOINTMENT_DETAILS = "SELECT ap.mr_no,ap.visit_id,"
      + " ap.patient_name,ap.patient_contact, ap.complaint,srt.category,"
      + " ap.remarks,ap.scheduler_visit_type, ap.presc_doc_id, (select doctor_name from doctors "
      + " where doctor_id=ap.presc_doc_id) as presc_doctor, #dept_id#, "
      + " #dept_name#, ap.center_id, ap.scheduler_prior_auth_no,ap.scheduler_prior_auth_mode_id,"
      + " #item_type#,  ap.appointment_id,ap.res_sch_id,ap.res_sch_name, "
      + " to_char(ap.appointment_time,'dd-MM-yyyy') AS text_appointment_date, "
      + " to_char(ap.appointment_time,'dd-MM-yyyy hh24:mi:ss') AS text_appointment_date_time, "
      + " to_char(ap.appointment_time,'hh24:mi')::time AS appointment_time, "
      + " to_char(ap.appointment_time,'hh24:mi') AS text_appointment_time,appointment_time "
      + " as appointment_date_time, to_char(appointment_time+(duration||' mins')::interval,"
      + " 'dd-MM-yyyy hh24:mi:ss') AS text_end_appointment_time, "
      + " to_char(appointment_time+(duration||' mins')::interval,'hh24:mi') AS "
      + " text_end_appointment_only_time, ap.duration,ap.appointment_status,ap.arrival_time, "
      + " ap.completed_time,ap.pat_package_id, ap.booked_by,ap.booked_time, cas.paid_at_source, "
      + " ap.changed_by,ap.changed_time,apit.resource_type, apit.resource_id, "
      + " apit.appointment_item_id,srt.primary_resource, #resource_name#, #central_resource_name#,"
      + " consultation_type_id, #mandate_additional_info#, #additional_info_reqts# "
      + " FROM scheduler_appointments ap "
      + " LEFT JOIN scheduler_appointment_items apit USING(appointment_id) "
      + " JOIN ( #MASTER_DATA# ) AS r ON (r.resource_id = sa.res_sch_name) "
      + " LEFT JOIN appointment_source_master cas ON "
      + " (cas.appointment_source_id = ap.app_source_id)"
      + " LEFT JOIN  scheduler_master  sm ON sm.res_sch_id = ap.res_sch_id "
      + " LEFT JOIN scheduler_resource_types srt ON (srt.category = sm.res_sch_category AND "
      + " apit.resource_type = srt.resource_type)";

  /** The Constant GET_DAY_APPOINTMENTS. */
  private static final String GET_DAY_APPOINTMENTS = "select sp.appointment_id, "
      + " sp.appointment_time,sp.duration,sp.appointment_status, sci.resource_id  "
      + " from scheduler_appointments sp JOIN scheduler_appointment_items sci "
      + " ON(sci.appointment_id=sp.appointment_id) "
      + " where resource_id = ? AND date(appointment_time) = ? "
      + " AND appointment_status not in ('Cancel','Noshow' ) ";

  private static final String GET_OVERBOOK_COUNT = "select count(*) as overbookcount "
      + " from scheduler_appointments "
      + " where prim_res_id=? and appointment_time=? and "
      + " appointment_status NOT IN ('Noshow','Cancel')";

  private static final String DOCTORS_OVERBOOKLIMIT = "SELECT overbook_limit from doctors "
      + "where doctor_id=?";
  private static final String TESTS_OVERBOOKLIMIT = "SELECT overbook_limit from "
      + "test_equipment_master where eq_id::text=?";
  private static final String SERVICES_OVERBOOKLIMIT = "SELECT overbook_limit "
      + "from service_resource_master where serv_res_id::text=?";
  private static final String THEATRES_OVERBOOKLIMIT = "SELECT overbook_limit from "
      + "theatre_master where theatre_id=?";

  /**
   * Get over_book count for the appointment slot.
   * @param resourceId the resource id
   * @param appointmentTime appointment time
   * @return integer number of appts for the slot
   */
  public int getOverBookCountForSlot(String resourceId, Timestamp appointmentTime) {
    String query = GET_OVERBOOK_COUNT;
    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(query,
        new Object[]{resourceId, appointmentTime});
    if (bean != null) {
      return Integer.valueOf(String.valueOf(bean.get("overbookcount")));
    }
    return 0;
  }
  /**
   * Get overbooklimit for the resource.
   * @param resourceId the resource id
   * @param category resource category
   * @return integer overbook limit of the resource
   */

  public int getResourceOverBookLimit(String resourceId, String category) {
    String query = "";
    if (category.equals("DOC")) {
      query = DOCTORS_OVERBOOKLIMIT;
    } else if (category.equals("DIA")) {
      query = TESTS_OVERBOOKLIMIT;
    } else if (category.equals("SNP")) {
      query = SERVICES_OVERBOOKLIMIT;
    } else if (category.equals("OPE")) {
      query = THEATRES_OVERBOOKLIMIT;
    }
    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(query, new Object[] {resourceId});
    if (bean != null && bean.get("overbook_limit") != null) {
      return (Integer) bean.get("overbook_limit");
    }
    return 0;
  }

  /**
   * Gets the overbook count for resource.
   *
   * @param startApptTime       the start appt time
   * @param endApptTime         the end appt time
   * @param primaryResource     the primary resource
   * @param primaryResourceType the primary resource type
   * @param appId               the app id
   * @return the overbook count for resource
   */
  public int getOverbookCountForResource(Timestamp startApptTime, Timestamp endApptTime,
      String primaryResource, String primaryResourceType, String appId) {
    // return DatabaseHelper.getInteger(GET_OVERBOOK_COUNT, primResId, apptTime);
    List<BasicDynaBean> apptList = isSlotBooked(startApptTime, endApptTime, appId, primaryResource,
        primaryResourceType);
    if (apptList == null) {
      return 0;
    }
    return apptList.size();
  }

  /**
   * Gets the next unique appoint ment ind.
   *
   * @return the next unique appoint ment ind
   */
  public int getNextUniqueAppointMentInd() {
    return DatabaseHelper.getInteger(GET_NEXT_UNIQUE_APPT_IND);
  }

  /**
   * Gets the next unique appointment package group id.
   *
   * @return the next unique appointment package group id
   */
  public int getNextUniqueAppointmentPackageGroupId() {
    return DatabaseHelper.getInteger(GET_NEXT_UNIQUE_APPOINTMENT_PACKAGE_GROUP_ID);
  }

  /**
   * Gets the appointments.
   *
   * @param appointmentCategory the appointment category
   * @param resourceIds         the resource ids
   * @param centerId            the center id
   * @param centersIncDefault   the centers inc default
   * @param startDate           the start date
   * @param endDate             the end date
   * @param modifiedAfter       the modified after
   * @param skipApptsWithStatus the skip appts with status
   * @param userName            the user name
   * @return the appointments
   */
  public List<BasicDynaBean> getAppointments(AppointmentCategory appointmentCategory,
      String[] resourceIds, Integer centerId, Integer centersIncDefault, Date startDate,
      Date endDate, Timestamp modifiedAfter, String[] skipApptsWithStatus, String userName) {

    String masterData = "";
    String masterDataFields = "";
    if (appointmentCategory != null) {
      masterData = appointmentCategory.getPrimaryResourceMasterQuery().replace("#CENTER_FILTER#",
          "");
      masterData = " JOIN ( " + masterData + " ) AS r ON (r.resource_id = sai.resource_id)";
      masterDataFields = "r.resource_name,r.dept_name,";
    }
    masterData = masterData.replace("#DEPT_FILTER#", "");
    String query = GET_APPOINTMENTS;
    query = query.concat(" WHERE (date(sa.appointment_time) >= :apptStartDate AND "
        + " date(sa.appointment_time) <= :apptEndDate AND "
        + "sai.resource_id IN (:resourceIds) #STATUS_FILTER# #CENTER_FILTER#"
        + " #MODIFIED_AFTER# )");
    query = query.replace("#MASTER_DATA#", masterData);
    query = query.replace("#MASTER_DATA_FIELDS#", masterDataFields);
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    if (centersIncDefault > 1 && centerId != 0) {
      query = query.replace("#CENTER_FILTER#", "AND sa.center_id = :centerId");
      parameters.addValue("centerId", centerId);
    } else {
      query = query.replace("#CENTER_FILTER#", "");
    }

    query = query.replace("#RESOURCE_FILTER#", "");
    if (modifiedAfter != null) {
      query = query.replace("#MODIFIED_AFTER#", "AND sa.changed_time >= :modifiedAfter");
      parameters.addValue("modifiedAfter", modifiedAfter);
    } else {
      query = query.replace("#MODIFIED_AFTER#", "");
    }

    if (skipApptsWithStatus != null) {
      query = query.replace("#STATUS_FILTER#",
          " AND (sa.appointment_status NOT IN (:apptStatus)) ");
      parameters.addValue("apptStatus", Arrays.asList(skipApptsWithStatus));
    } else {
      query = query.replace("#STATUS_FILTER#", "");
    }
    query = query.replace("#EMPUSERNAMEFILTER#", " AND emp_username = :empUsername");
    query = query.replace("#EMPUSERNAMEFILTERFORMRNO#", " AND uma.emp_username = :empUsername");
    parameters.addValue("empUsername", userName);
    parameters.addValue("empUsername", userName);
    parameters.addValue("apptStartDate", startDate);
    parameters.addValue("apptEndDate", endDate);
    parameters.addValue("resourceIds", Arrays.asList(resourceIds));
    return DatabaseHelper.queryToDynaList(query, parameters);
  }

  /**
   * Gets the appointment details.
   *
   * @param appointmentCategory the appointment category
   * @param appointmentId       the appointment id
   * @return the appointment details
   */
  public List<BasicDynaBean> getAppointmentDetails(AppointmentCategory appointmentCategory,
      int appointmentId) {

    String query = "select * from (" + appointmentCategory.getAppointmentDetailsQuery()
        + " ) as appointment ";
    query = query.replace("#CENTER_FILTER#", "");
    query = query.replace("#DOCTOR_FILTER#", "");
    query = query.replace("#DEPT_FILTER#", "");
    query = query.replace("#RESOURCE_FILTER#", "");
    StringBuilder queryStr = new StringBuilder(query);
    queryStr.append(" where appointment_id = :apptId ");
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("apptId", appointmentId);
    return DatabaseHelper.queryToDynaList(queryStr.toString(), parameters);

  }

  /**
   * Checks if is slot booked.
   *
   * @param startApptTime       the start appt time
   * @param endApptTime         the end appt time
   * @param appointmentId       the appointment id
   * @param primaryResource     the primary resource
   * @param primaryResourceType the primary resource type
   * @return the list
   */
  public List<BasicDynaBean> isSlotBooked(Timestamp startApptTime, Timestamp endApptTime,
      String appointmentId, String primaryResource, String primaryResourceType) {
    return isSlotBooked(startApptTime, endApptTime, appointmentId, primaryResource,
        primaryResourceType, null);
  }
  
  
  /**
   * Checks if is slot booked.
   *
   * @param startApptTime the start appt time
   * @param endApptTime the end appt time
   * @param appointmentId the appointment id
   * @param primaryResource the primary resource
   * @param primaryResourceType the primary resource type
   * @param waitlistNumber the waitlist number
   * @return the list
   */
  public List<BasicDynaBean> isSlotBooked(Timestamp startApptTime, Timestamp endApptTime,
      String appointmentId, String primaryResource, String primaryResourceType,
      Integer waitlistNumber) {
    String query = CHECK_IF_SLOT_IS_BOOKED;
    if (primaryResourceType.endsWith("DOC") || primaryResourceType.equalsIgnoreCase("LABTECH")) {
      query = query.replace("#resource_match#",
          " foo.resource_type in ('DOC','OPDOC','SUDOC','ANEDOC','ASUDOC','PAEDOC','LABTECH') ");
    } else {
      query = query.replace("#resource_match#", " foo.resource_type = :primResType ");
    }
    if (waitlistNumber != null) {
      query = query.replace("#waitlist", "AND foo.waitlist > :waitList");
    } else {
      query = query.replace("#waitlist", "");
    }
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("startApptTime", startApptTime);
    parameters.addValue("endApptTime", endApptTime);
    if (appointmentId != null && !appointmentId.equals("") && !appointmentId.equals("No")) {
      parameters.addValue("appointmentId", Integer.parseInt(appointmentId));
    } else {
      parameters.addValue("appointmentId", -1);
    }
    parameters.addValue("primResType", primaryResourceType);
    parameters.addValue("primResId", primaryResource);
    if (waitlistNumber != null) {
      parameters.addValue("waitList", waitlistNumber);
    }
    return DatabaseHelper.queryToDynaList(query, parameters);
  }

  /** The get all appointments. */
  private static String GET_ALL_APPOINTMENTS = "SELECT appointment_time, "
      + "visit_mode, appointment_time +"
      + " (duration||' mins')::interval AS end_appointment_time "
      + " FROM scheduler_appointments sa  "
      + " JOIN scheduler_appointment_items sai using(appointment_id) "
      + " WHERE appointment_status NOT IN('Cancel','Noshow') " + "AND #resource_match# "
      + " AND sai.resource_id = :resId "
      + " AND to_char(sa.appointment_time, 'DD-MM-YYYY') = :date :: text #discard_apptid#";

  /**
   * Gets the all appts for day for res.
   *
   * @param dateStr the date str
   * @param resType the res type
   * @param resId   the res id
   * @return the all appts for day for res
   */
  public List<BasicDynaBean> getAllApptsForDayForRes(String dateStr, String resType, String resId,
      String apptid) {
    String query = GET_ALL_APPOINTMENTS;
    if (resType.endsWith("DOC") || resType.equalsIgnoreCase("LABTECH")) {
      query = query.replace("#resource_match#",
          " sai.resource_type in ('DOC','OPDOC','SUDOC','ANEDOC','ASUDOC','PAEDOC','LABTECH') ");
    } else {
      query = query.replace("#resource_match#", " sai.resource_type = :resType ");
    }
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("date", dateStr);
    parameters.addValue("resType", resType);
    parameters.addValue("resId", resId);
    if (apptid != null) {
      query = query.replace("#discard_apptid#", " AND appointment_id !=  :apptid ");
      parameters.addValue("apptid", Integer.parseInt(apptid));
    } else {
      query = query.replace("#discard_apptid#", " ");
    }

    return DatabaseHelper.queryToDynaList(query, parameters);
  }

  /**
   * Appointment exists.
   *
   * @param startApptTime  the start appt time
   * @param endApptTime    the end appt time
   * @param appointmentId  the appointment id
   * @param mrNo           the mr no
   * @param patientName    the patient name
   * @param patientContact the patient contact
   * @param contactId      the contact id
   * @return the list
   */
  public List<BasicDynaBean> appointmentExists(Timestamp startApptTime, Timestamp endApptTime,
      String appointmentId, String mrNo, String patientName, String patientContact,
      Integer contactId) {
    String query = CHECK_IF_APPOINTMENT_EXISTS_FOR_PATIENT_;
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("startApptTime", startApptTime);
    parameters.addValue("endApptTime", endApptTime);
    if (appointmentId != null && !appointmentId.equals("") && !appointmentId.equals("No")) {
      parameters.addValue("appointmentId", Integer.parseInt(appointmentId));
    } else {
      parameters.addValue("appointmentId", -1);
    }
    if (mrNo == null || mrNo.equals("")) {
      if (contactId == null) {
        query = query.replace("#",
            "AND patient_name = :patientName AND patient_contact = :patientContact");
        parameters.addValue("patientName", patientName);
        parameters.addValue("patientContact", patientContact);
      } else {
        query = query.replace("#", "AND contact_id = :contactId ");
        parameters.addValue("contactId", contactId);
      }

    } else {
      query = query.replace("#", "AND mr_no = :mrNo ");
      parameters.addValue("mrNo", mrNo);
    }
    return DatabaseHelper.queryToDynaList(query, parameters);
  }

  /**
   * Appointment exists for same package.
   *
   * @param startApptTime  the start appt time
   * @param endApptTime    the end appt time
   * @param appointmentId  the appointment id
   * @param mrNo           the mr no
   * @param patientName    the patient name
   * @param patientContact the patient contact
   * @param packageId      the package id
   * @param groupId        the group id
   * @return the list
   */
  public List<BasicDynaBean> appointmentExistsForSamePackage(Timestamp startApptTime,
      Timestamp endApptTime, String appointmentId, String mrNo, String patientName,
      String patientContact, int packageId, int groupId) {
    String query = CHECK_IF_APPOINTMENT_EXISTS_FOR_PATIENT_FOR_SAME_PACKAGE;
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("startApptTime", startApptTime);
    parameters.addValue("endApptTime", endApptTime);
    if (appointmentId != null && !appointmentId.equals("") && !appointmentId.equals("No")) {
      parameters.addValue("appointmentId", Integer.parseInt(appointmentId));
    } else {
      parameters.addValue("appointmentId", -1);
    }
    if (mrNo == null || mrNo.equals("")) {
      query = query.replace("#",
          "AND patient_name = :patientName AND patient_contact = :patientContact");
      parameters.addValue("patientName", patientName);
      parameters.addValue("patientContact", patientContact);
    } else {
      query = query.replace("#", "AND mr_no = :mrNo ");
      parameters.addValue("mrNo", mrNo);
    }
    parameters.addValue("groupId", groupId);
    parameters.addValue("packageId", packageId);
    return DatabaseHelper.queryToDynaList(query, parameters);
  }

  /** The Constant GetAppointmentSourceName. */
  private static final String GetAppointmentSourceName = " select casm.appointment_source_name "
      + " from appointment_source_master casm "
      + " join scheduler_appointments sa on(casm.appointment_source_id=sa.app_source_id) "
      + " where appointment_id=? ";

  /**
   * Gets the appointment source.
   *
   * @param appointmentId the appointment id
   * @return the appointment source
   */
  public String getAppointmentSource(Integer appointmentId) {
    return DatabaseHelper.getString(GetAppointmentSourceName, new Object[] { appointmentId });
  }

  /** The Constant GET_APPOINTMENTS_FOR_A_RESOURCE. */
  private static final String GET_APPOINTMENTS_FOR_A_RESOURCE = "SELECT * FROM "
      + " (SELECT sa.*,sai.resource_id,appointment_time+(duration||' mins')::interval "
      + " AS end_appointment_time " + " FROM scheduler_appointments sa "
      + " JOIN scheduler_appointment_items sai ON(sa.appointment_id = sai.appointment_id) # )as foo"
      + " WHERE resource_id::text = ? " + " AND appointment_status NOT IN('Cancel','Noshow') "
      + " AND ((appointment_time <= ? AND end_appointment_time > ?) "
      + " OR  (appointment_time >= ? AND appointment_time < ?)) "
      + " ORDER BY end_appointment_time ";

  /** The Constant DOCTOR_QUERY. */
  public static final String DOCTOR_QUERY = " JOIN doctors res ON"
      + "(res.doctor_id = sai.resource_id)";

  /** The Constant EQUIPMENT_QUERY. */
  public static final String EQUIPMENT_QUERY = " JOIN test_equipment_master res "
      + "ON(res.eq_id::text = sai.resource_id)";

  /** The Constant SERVICE_RESOURCES_QUERY. */
  public static final String SERVICE_RESOURCES_QUERY = " JOIN service_resource_master "
      + "res ON(res.serv_res_id::text = sai.resource_id)";

  /** The Constant THEATRE_QUERY. */
  public static final String THEATRE_QUERY = " JOIN theatre_master res "
      + "ON(res.theatre_id = sai.resource_id)";

  /** The Constant GET_APPOINTMENT_DETAILS_BY_APPOINTMENT_ID. */
  public static final String GET_APPOINTMENT_DETAILS_BY_APPOINTMENT_ID = "SELECT * FROM "
      + "scheduler_appointments where appointment_id = ? ";

  /** The Constant GET_APPOINTMENT_ITEMS_BY_APPOINTMENT_ID. */
  public static final String GET_APPOINTMENT_ITEMS_BY_APPOINTMENT_ID = "SELECT * FROM "
      + "scheduler_appointment_items where appointment_id = ? ";

  /**
   * Gets the resource appointments.
   *
   * @param resourceId   the resource id
   * @param resourceType the resource type
   * @param startTime    the start time
   * @param endTime      the end time
   * @return the resource appointments
   */
  public List<BasicDynaBean> getResourceAppointments(String resourceId, String resourceType,
      Timestamp startTime, Timestamp endTime) {
    String query = new String(GET_APPOINTMENTS_FOR_A_RESOURCE);
    if (resourceType.equals("DOC")) {
      query = query.replace("#", DOCTOR_QUERY);
    } else if (resourceType.equals("OPE")) {
      query = query.replace("#", THEATRE_QUERY);
    } else if (resourceType.equals("DIA")) {
      query = query.replace("#", EQUIPMENT_QUERY);
    } else if (resourceType.equals("SNP")) {
      query = query.replace("#", SERVICE_RESOURCES_QUERY);
    }
    return DatabaseHelper.queryToDynaList(query,
        new Object[] { resourceId, startTime, startTime, startTime, endTime });
  }

  /** The Constant GET_APPOINTMENTS_FOR_A_RESOURCE. */
  private static final String APPOINTMENTS_EXISTS_FOR_A_RESOURCE = "Select exists("
        + " select 1 from scheduler_appointments sa JOIN scheduler_appointment_items sai ON"
        + "(sa.appointment_id = sai.appointment_id)"
        + " where sai.resource_id::text = ? and sa.appointment_time >= ? and "
        + " appointment_status NOT IN('Cancel','Noshow') and sa.visit_mode = ?)";
  
  /**
   * Online appointments exists.
   *
   * @param resourceId the resource id
   * @param currentDateTime the current date time
   * @param visitMode the visit mode
   * @return true, if successful
   */
  public boolean onlineAppointmentsExists(String resourceId, 
        Timestamp currentDateTime, String visitMode) {
    String query = APPOINTMENTS_EXISTS_FOR_A_RESOURCE;
    Object[] params = new Object[] { resourceId, currentDateTime, visitMode };
    return DatabaseHelper.getBoolean(query,params);
  }

  /**
   * Gets the scheduler appts.
   *
   * @param appointmentId the appointment id
   * @return the scheduler appts
   */
  public List<BasicDynaBean> getSchedulerAppts(Integer appointmentId) {
    return DatabaseHelper.queryToDynaList(
        "select * from scheduler_appointment_items where appointment_id = ? ",
        new Object[] { appointmentId });
  }

  /**
   * Adds the scheduler appointment items.
   *
   * @param insertList the insert list
   * @param userName   the user name
   * @param apptId     the appt id
   * @return true, if successful
   */
  @SuppressWarnings("rawtypes")
  public boolean addSchedulerAppointmentItems(List<Map> insertList, String userName, int apptId) {

    boolean result = true;
    List<BasicDynaBean> dynaList = new ArrayList<>();
    for (Map row : insertList) {

      BasicDynaBean bean = appointmentItemsRepository.getBean();
      bean.set("appointment_id", apptId);
      bean.set("resource_id", row.get("resource_id"));
      bean.set("resource_type", row.get("resource_type"));
      bean.set("appointment_item_id", appointmentItemsRepository.getNextSequence());
      bean.set("mod_time", DateUtil.getCurrentTimestamp());
      bean.set("user_name", userName);
      dynaList.add(bean);

    }
    int[] successes = appointmentItemsRepository.batchInsert(dynaList);
    for (int success : successes) {

      if (success < 0) {
        result = false;
        break;
      }
    }
    return result;
  }

  /**
   * Adds the scheduler appointment items.
   *
   * @param insertList the insert list
   * @return true, if successful
   */
  public boolean addSchedulerAppointmentItems(List<AppointmentResource> insertList) {

    boolean result = true;
    List<BasicDynaBean> dynaList = new ArrayList<>();
    for (AppointmentResource row : insertList) {

      BasicDynaBean bean = appointmentItemsRepository.getBean();
      bean.set("appointment_id", row.getAppointmentId());
      bean.set("resource_id", row.getResourceId());
      bean.set("resource_type", row.getResourceType());
      bean.set("appointment_item_id", row.getAppointment_item_id());
      bean.set("mod_time", row.getMod_time());
      bean.set("user_name", row.getUser_name());
      dynaList.add(bean);

    }
    int[] successes = appointmentItemsRepository.batchInsert(dynaList);
    for (int success : successes) {

      if (success < 0) {
        result = false;
        break;
      }
    }
    return result;
  }

  /**
   * Removes the scheduler appointment items.
   *
   * @param deleteList the delete list
   * @param apptId     the appt id
   * @return true, if successful
   */
  @SuppressWarnings("rawtypes")
  public boolean removeSchedulerAppointmentItems(List<Map> deleteList, Integer apptId) {
    boolean result = true;
    for (Map row : deleteList) {

      Map<String, Object> deleteMap = new HashMap<>();
      deleteMap.put("appointment_id", apptId);
      deleteMap.put("resource_id", row.get("resource_id"));
      if (appointmentItemsRepository.delete(deleteMap) != 1) {
        result = false;
        break;
      }
    }
    return result;

  }

  /**
   * Gets the scheduler items by item id.
   *
   * @param apptId the appt id
   * @return the scheduler items by item id
   */
  public List<BasicDynaBean> getSchedulerItemsByItemId(Integer apptId) {

    return appointmentItemsRepository.listAll(Collections.<String>emptyList(), "appointment_id",
        apptId);
  }

  /**
   * Gets the appointments by appointment id.
   *
   * @param apptCategory  the appt category
   * @param appointmentId the appointment id
   * @param userName      the user name
   * @return the appointments by appointment id
   */
  public BasicDynaBean getAppointmentsByAppointmentId(AppointmentCategory apptCategory,
      Integer appointmentId, String userName) {
    if (apptCategory != null) {
      String masterData = apptCategory.getPrimaryResourceMasterQuery()
          .replace("#CENTER_FILTER# #RESOURCE_FILTER# #DEPT_FILTER#", "");
      String query = GET_APPOINTMENTS;
      query = query.concat(" WHERE sa.appointment_id = ?");
      query = query.replace("#MASTER_DATA#",
          " JOIN ( " + masterData + " ) AS r ON (r.resource_id = sai.resource_id)");
      query = query.replace("#MASTER_DATA_FIELDS#", "r.resource_name,r.dept_name,");
      if (userName != null) {
        query = query.replace("#EMPUSERNAMEFILTER#", " AND emp_username = ? ");
        query = query.replace("#EMPUSERNAMEFILTERFORMRNO#", " AND uma.emp_username = :empUsername");
        return DatabaseHelper.queryToDynaBean(query, userName, userName, appointmentId);
      } else {
        query = query.replace("#EMPUSERNAMEFILTER#", " ");
        query = query.replace("#EMPUSERNAMEFILTERFORMRNO#", " ");
        return DatabaseHelper.queryToDynaBean(query, new Object[] { appointmentId });
      }

    } else {
      return null;
    }
  }

  /** The Constant APPOINTMENT_COMPLETED. */
  private static final String APPOINTMENT_COMPLETED = "UPDATE scheduler_appointments SET "
      + "appointment_status=? , completed_time=? WHERE appointment_id=?";

  /**
   * Update appointments.
   *
   * @param appointmentId the appointment id
   * @return true, if successful
   */
  public boolean updateAppointments(int appointmentId) {

    boolean success = true;
    success = DatabaseHelper.update(APPOINTMENT_COMPLETED, new Object[] { "Completed",
        new java.sql.Timestamp((new java.util.Date()).getTime()), appointmentId }) > 0;

    return success;
  }

  /** The Constant SPONSOR_INFO. */
  public static final String SPONSOR_INFO = "Select status from tpa_master where tpa_id = ?";

  /**
   * Gets the sponsor bean.
   *
   * @param sponsorId the sponsor id
   * @return the sponsor bean
   */
  public BasicDynaBean getSponsorBean(String sponsorId) {
    String query = SPONSOR_INFO;
    return DatabaseHelper.queryToDynaBean(query, new Object[] { sponsorId });
  }

  /**
   * Validate if res exists.
   *
   * @param query the query
   * @param resId the res id
   * @return the basic dyna bean
   * @throws DataIntegrityViolationException the data integrity violation
   *                                         exception
   */
  public BasicDynaBean validateIfResExists(String query, String resId)
      throws DataIntegrityViolationException {
    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(query, new Object[] { resId });
    return bean;
  }

  /** The get appointments for patient. */
  private static String GET_APPOINTMENTS_FOR_PATIENT = "Select "
      + " sa.appointment_id,sa.appointment_time :: date as date,sa.appointment_time :: time "
      + " as start_time ,sa.duration,sa.center_id,sa.appointment_status,sa.package_id,"
      + " p.package_name, hcm.center_name,sa.complaint, sa.patient_contact, "
      + " sa.res_sch_name AS sec_res_id, sa.patient_name, sa.appointment_pack_group_id,"
      + " sa.patient_name,sa.remarks, sa.waitlist::text, "
      + " asm.appointment_source_name as app_source_name, "
      + " sa.app_source_id, asm.appointment_source_name, sa.scheduler_visit_type, "
      + " sa.presc_doc_id, (select doctor_name from doctors where doctor_id = presc_doc_id)"
      + " as presc_doc_name,sa.cond_doc_id, (select doctor_name from doctors where"
      + " doctor_id = cond_doc_id) as cond_doc_name, sa.rescheduled, sa.visit_mode, "
      + " sa.changed_time :: date as changed_date, sa.changed_time :: time as changed_time,"
      + " sa.orig_appt_time :: date as original_date,sa.orig_appt_time :: time as original_time,"
      + " CASE WHEN res_sch_category='DIA' THEN dig.conducting_doc_mandatory "
      + " ELSE null END as conducting_doc_mandatory,"
      + " CASE WHEN res_sch_category='DIA' then dig.test_name"
      + "     WHEN res_sch_category='SNP' then ser.service_name"
      + "     WHEN res_sch_category='OPE' then opm.operation_name"
      + "     WHEN res_sch_category='DOC' then (select consultation_type from consultation_types "
      + " where consultation_type_id :: text  = sa.res_sch_name) " + " END as sec_res_name,"
      + " CASE WHEN ddept.category = 'DEP_LAB' THEN 'Laboratory' "
      + "     WHEN  ddept.category = 'DEP_RAD' THEN 'Radiology' END AS type, "
      + " CASE WHEN res_sch_category='DIA' then ddept.ddept_id"
      + "     WHEN res_sch_category='SNP' then sd.serv_dept_id::text"
      + "     WHEN res_sch_category='DOC' then docdept.dept_id END as department_id, "
      + " CASE WHEN res_sch_category='DIA' then ddept.ddept_name"
      + "     WHEN res_sch_category='SNP' then sd.department "
      + "     WHEN res_sch_category='DOC' then docdept.dept_name END as department_name,"
      + " sa.prim_res_id,"
      + " CASE WHEN res_sch_category='DIA' then (select equipment_name from test_equipment_master "
      + " where eq_id :: text =sa.prim_res_id) "
      + "     WHEN res_sch_category='SNP' then (select serv_resource_name from "
      + " service_resource_master where serv_res_id :: text =sa.prim_res_id) "
      + "     WHEN res_sch_category='DOC' then (select doctor_name from doctors "
      + " where doctor_id = sa.prim_res_id) "
      + "     WHEN res_sch_category='OPE' then (select theatre_name from theatre_master "
      + " where theatre_id = sa.prim_res_id) "
      + " END as prim_res_name,res_sch_category as app_cat, "
      + " sai.resource_id as add_res_id ,sai.resource_type as add_res_type, "
      + " CASE WHEN sai.resource_type='EQID' then (select equipment_name from "
      + "test_equipment_master  where eq_id :: text =sai.resource_id) "
      + " WHEN  sai.resource_type='SRID' then (select serv_resource_name from "
      + " service_resource_master where serv_res_id :: text =sai.resource_id) "
      + " WHEN  sai.resource_type in ('ASUDOC','LABTECH','SUDOC','ANEDOC','OPDOC','DOC','PAEDDOC')"
      + " then (select doctor_name from doctors  where doctor_id = sai.resource_id)      "
      + " WHEN  sai.resource_type='THID' then (select theatre_name from theatre_master "
      + " where theatre_id = sai.resource_id) "
      + " WHEN  sai.resource_type='SUR' then (select operation_name from operation_master "
      + " where op_id = sai.resource_id) "
      + " WHEN  sai.resource_type='SER' then (select service_name from services "
      + " where service_id = sai.resource_id) "
      + " WHEN  sai.resource_type='TST' then (select test_name from diagnostics "
      + " where test_id = sai.resource_id)"
      + " WHEN  sai.resource_type in (select scheduler_resource_type from generic_resource_type) "
      + " then (select generic_resource_name from generic_resource_master where "
      + " generic_resource_id::text = sai.resource_id) "
      + "  END as add_res_name "
      + " FROM scheduler_appointments sa  "
      + " JOIN scheduler_appointment_items sai ON (sa.appointment_id = sai.appointment_id) "
      + " JOIN scheduler_master sm ON (sm.res_sch_id = sa.res_sch_id) "
      + " JOIN hospital_center_master hcm ON(hcm.center_id=sa.center_id) "
      + " LEFT JOIN packages p ON (p.package_id = sa.package_id) "
      + " LEFT JOIN appointment_source_master asm ON "
      + " (asm.appointment_source_id = sa.app_source_id) "
      + " LEFT JOIN diagnostics dig ON (dig.test_id=sa.res_sch_name AND sm.res_sch_category='DIA')"
      
      + " LEFT JOIN doctors doc ON (doc.doctor_id = sa.prim_res_id)"
      + " LEFT JOIN department docdept ON (doc.dept_id = docdept.dept_id)"
      
      + " LEFT JOIN diagnostics_departments ddept ON (ddept.ddept_id = dig.ddept_id)"
      + " LEFT JOIN services ser ON (ser.service_id=sa.res_sch_name AND sm.res_sch_category='SNP')"
      + " LEFT JOIN services_departments sd ON (sd.serv_dept_id = ser.serv_dept_id)"
      + " LEFT JOIN operation_master opm ON(opm.op_id=sa.res_sch_name"
      + " AND sm.res_sch_category='OPE')"
      + " #PRIMARY_FILTER# #TIME_FILTER# #CENTER_FILTER# #STATUS_FILTER# "
      + " ORDER BY sa.appointment_time :: date ASC";

  /** The Constant CENTER_FILTER. */
  private static final String CENTER_FILTER = " AND sa.center_id IN (:centers)";

  /** The Constant TIME_FILTER. */
  private static final String TIME_FILTER = " AND sa.appointment_time >= :fromDate";

  /** The Constant MR_NO_FILTER. */
  private static final String MR_NO_FILTER = " WHERE sa.mr_no = :mrNo";

  /** The Constant CONTACT_ID_FILTER. */
  private static final String CONTACT_ID_FILTER = "WHERE sa.contact_id = :contactId";
  
  /** The Constant PHONE_NUMBER_FILTER. */
  private static final String PHONE_NUMBER_FILTER = "WHERE sa.patient_contact IN "
      + "(:patientContact , :nationalPart)";

  /** The Constant APPOINTMENT_ID_FILTER. */
  private static final String APPOINTMENT_ID_FILTER = " WHERE sa.appointment_id = :appointmentId";

  /** The Constant STATUS_FILTER_ACTIVE. */
  private static final String STATUS_FILTER_ACTIVE = " AND sa.appointment_status IN "
      + "('Booked','confirmed','Confirmed')";

  /** The Constant STATUS_FILTER_INACTIVE. */
  private static final String STATUS_FILTER_INACTIVE = " AND sa.appointment_status NOT IN "
      + "('Booked','confirmed','Confirmed')";
  
  /** The Constant PHONE_WITHOUT_COUNTRY_CODE_FILTER. */
  private static final String PHONE_WITHOUT_COUNTRY_CODE_FILTER = "WHERE REPLACE"
      + "(sa.patient_contact,sa.patient_contact_country_code,'') IN (:patientContact) ";

  /**
   * Gets the appointments for patient.
   *
   * @param centerIds  the center ids
   * @param fromDate   the from date
   * @param mrNo       the mr no
   * @param contactId  the contact id
   * @param patientContact the patient contact
   * @param appId      the app id
   * @param apptStatus the appt status
   * @return the appointments for patient
   */
  public List<BasicDynaBean> getAppointmentsForPatient(List<Integer> centerIds, Timestamp fromDate,
      String mrNo, Integer contactId, String patientContact, Integer appId, String apptStatus) {

    String query = GET_APPOINTMENTS_FOR_PATIENT;
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    if (appId != null) {
      query = query.replace("#PRIMARY_FILTER# #TIME_FILTER# #CENTER_FILTER# #STATUS_FILTER#",
          APPOINTMENT_ID_FILTER);
      parameters.addValue("appointmentId", appId);
    } else {
      if (contactId != null) {
        query = query.replace("#PRIMARY_FILTER#", CONTACT_ID_FILTER);
        parameters.addValue("contactId", contactId);
      } else if (mrNo != null) {
        query = query.replace("#PRIMARY_FILTER#", MR_NO_FILTER);
        parameters.addValue("mrNo", mrNo);
      } else {
        String phoneNumberNationalPart = PhoneNumberUtil.getNationalNumber(patientContact);
        if (phoneNumberNationalPart == null) {
          query = query.replace("#PRIMARY_FILTER#", PHONE_WITHOUT_COUNTRY_CODE_FILTER);
          parameters.addValue("patientContact", patientContact);
        } else {
          query = query.replace("#PRIMARY_FILTER#", PHONE_NUMBER_FILTER);
          parameters.addValue("patientContact", patientContact);
          parameters.addValue("nationalPart", phoneNumberNationalPart);
        }        
      }
      if (centerIds != null && !centerIds.contains(0)) {
        query = query.replace("#CENTER_FILTER#", CENTER_FILTER);
        parameters.addValue("centers", centerIds);
      } else {
        query = query.replace("#CENTER_FILTER#", "");
      }
      if (fromDate != null) {
        query = query.replace("#TIME_FILTER#", TIME_FILTER);
        parameters.addValue("fromDate", fromDate);
      } else {
        query = query.replace("#TIME_FILTER#", "");
      }
      switch (apptStatus.toLowerCase()) {
        case "all":
          query = query.replace("#STATUS_FILTER#", "");
          break;
        case "active":
          query = query.replace("#STATUS_FILTER#", STATUS_FILTER_ACTIVE);
          break;
        case "inactive":
          query = query.replace("#STATUS_FILTER#", STATUS_FILTER_INACTIVE);
          break;
        default:
          query = query.replace("#STATUS_FILTER#", "");
          break;
      }
    }

    return DatabaseHelper.queryToDynaList(query, parameters);
  }

  /** The Constant GET_APPOIMTNET_DATE_COUNT. */
  public static final String GET_APPOIMTNET_DATE_COUNT = "select count(sp.appointment_time),"
      + " sp.appointment_time::time  as time, sp.duration, sp.center_id from"
      + " scheduler_appointments sp JOIN scheduler_appointment_items sci"
      + " ON(sci.appointment_id=sp.appointment_id)  where resource_id = ? AND "
      + " date(appointment_time) = ? AND appointment_status not in ('Cancel','Noshow' )"
      + " group by appointment_time,duration,center_id ";

  /**
   * Gets the appointment count and time.
   *
   * @param resourceId the resource id
   * @param date       the date
   * @return the appointment count and time
   */
  public List<BasicDynaBean> getAppointmentCountAndTime(String resourceId, Date date) {
    return DatabaseHelper.queryToDynaList(GET_APPOIMTNET_DATE_COUNT, resourceId, date);
  }

  /** The Constant GET_PREVIOUS_APPOINTMEN_DEATILS. */
  private static final String GET_PREVIOUS_APPOINTMEN_DEATILS = " SELECT mr_no, appointment_id,"
      + " appointment_status, sa.visit_mode, appointment_time as appointment_datetime,"
      + " CASE WHEN sm.res_sch_category = 'DOC' THEN doc.doctor_name"
      + " WHEN sm.res_sch_category = 'SNP' THEN ser.service_name"
      + " WHEN sm.res_sch_category = 'DIA' THEN diag.test_name"
      + " WHEN sm.res_sch_category = 'OPE' THEN ope.operation_name" + " END AS resource_name,"
      + " CASE WHEN sm.res_sch_category = 'DOC' THEN 'Consultation'"
      + " WHEN sm.res_sch_category = 'SNP' THEN 'Service'"
      + " WHEN sm.res_sch_category = 'DIA' THEN 'Test'"
      + " WHEN sm.res_sch_category = 'OPE' THEN 'Surgery'" + " END AS schedule_type "
      + " FROM scheduler_appointments sa"
      + " LEFT JOIN scheduler_master sm ON (sm.res_sch_id = sa.res_sch_id)"
      + " LEFT JOIN doctors doc ON(sa.prim_res_id = doc.doctor_id)"
      + " LEFT JOIN operation_master ope ON(sa.res_sch_name = ope.op_id)"
      + " LEFT JOIN diagnostics diag ON(sa.res_sch_name = diag.test_id)"
      + " LEFT JOIN services ser ON(sa.res_sch_name = ser.service_id)"
      + " WHERE mr_no = ? AND appointment_id != ? ORDER BY appointment_datetime DESC, "
      + "appointment_id DESC LIMIT 1";

  /**
   * Gets the previous appt details.
   *
   * @param mrNo          the mr no
   * @param appointmentId the appointment id
   * @return the previous appt details
   */
  public BasicDynaBean getPreviousApptDetails(String mrNo, Integer appointmentId) {
    String query = GET_PREVIOUS_APPOINTMEN_DEATILS;
    return DatabaseHelper.queryToDynaBean(query, mrNo, appointmentId);
  }

  /** The Constant GET_APPOINTMENT_CATEGORY. */
  private static final String GET_APPOINTMENT_CATEGORY = "select res_sch_category"
      + " from scheduler_master where res_sch_id = ? ";

  /**
   * Gets the appointment category.
   *
   * @param resSchId the res sch id
   * @return the appointment category
   */
  public String getAppointmentCategory(Integer resSchId) {
    String query = GET_APPOINTMENT_CATEGORY;
    return DatabaseHelper.getString(query, resSchId);
  }

  /** The Constant GET_MR_NO_FOR_APPOINTMENT. */
  private static final String GET_MR_NO_FOR_APPOINTMENT = "Select mr_no from "
      + "scheduler_appointments where appointment_id = ?";

  /**
   * Gets the mr no for appointment.
   *
   * @param appointmentId the appointment id
   * @return the mr no for appointment
   */
  public BasicDynaBean getMrNoForAppointment(Integer appointmentId) {
    return DatabaseHelper.queryToDynaBean(GET_MR_NO_FOR_APPOINTMENT,
        new Object[] { appointmentId });
  }

  /** The Constant GET_FILTERED_CTY. */
  // TODO : move to correct repository and call using service.
  private static final String GET_FILTERED_CTY = "SELECT pcm.consultation_type_id "
      + " FROM practitioner_type_consultation_mapping pcm "
      + " JOIN doctors d ON (d.practitioner_id = pcm.practitioner_type_id) "
      + " JOIN practitioner_types pt ON (pt.practitioner_id = pcm.practitioner_type_id) "
      + " WHERE d.doctor_id = ? AND pt.status = 'A'";

  /**
   * Gets the filtered consultation types.
   *
   * @param docId the doc id
   * @return the filtered consultation types
   */
  public List<BasicDynaBean> getFilteredConsultationTypes(String docId) {
    String query = GET_FILTERED_CTY;
    return DatabaseHelper.queryToDynaList(query, docId);
  }

  /** The Constant GET_APPOINTMENT_COUNT. */
  private static final String GET_APPOINTMENT_COUNT = "select count(distinct(case when "
      + " appointment_pack_group_id !=0  then appointment_pack_group_id else appointment_id end)) "
      + " from scheduler_appointments where date(appointment_time) = ? AND"
      + " appointment_status not in ('Cancel','Noshow' ) # ";

  /**
   * Checks if is appointment limit reached.
   *
   * @param apptDate  the appt date
   * @param mrNo      the mr no
   * @param phoneNo   the phone no
   * @param apptLimit the appt limit
   * @return true, if is appointment limit reached
   */
  public boolean isAppointmentLimitReached(Date apptDate, String mrNo, String phoneNo,
      int apptLimit) {
    // limit as -1 means there is no limit
    if (apptLimit == -1) {
      return false;
    }
    String phoneClause = "AND patient_contact = ? ";
    String mrnoClause = "AND mr_no = ?";
    String appointmentCountQuery = GET_APPOINTMENT_COUNT;
    Object[] params;
    if (mrNo == null || mrNo.equals("")) {
      appointmentCountQuery = appointmentCountQuery.replace("#", phoneClause);
      params = new Object[] { apptDate, phoneNo };
    } else {
      appointmentCountQuery = appointmentCountQuery.replace("#", mrnoClause);
      params = new Object[] { apptDate, mrNo };
    }
    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(appointmentCountQuery, params);

    return (bean != null && bean.get("count") != null && ((Long) bean.get("count") >= apptLimit));
  }

  /** The Constant UPDATE_APP_ON_CONTACT_EDIT. */
  private static final String UPDATE_APP_ON_CONTACT_EDIT = "update scheduler_appointments "
      + "set patient_name = ? ," + " patient_contact = ? ," + " patient_contact_country_code = ? ,"
      + " vip_status = ? " + " where contact_id = ?";

  /**
   * Update apps on contact edit.
   *
   * @param patientName    the patient name
   * @param patientContact the patient contact
   * @param countryCode    the country code
   * @param vipStatus      the vip status
   * @param contactId      the contact id
   */
  public void updateAppsOnContactEdit(String patientName, String patientContact, String countryCode,
      String vipStatus, Integer contactId) {

    DatabaseHelper.update(UPDATE_APP_ON_CONTACT_EDIT,
        new Object[] { patientName, patientContact, countryCode, vipStatus, contactId });
  }

  /** The Constant GET_SCHEDULE_DETAILS_FOR_PENDING_PRESC. */
  private static final String GET_SCHEDULE_DETAILS_FOR_PENDING_PRESC = "SELECT "
      + "appointment_id, appointment_status, appointment_time, patient_presc_id,duration "
      + "FROM scheduler_appointments WHERE appointment_id IN ("
      + "SELECT MAX(appointment_id) FROM scheduler_appointments "
      + "WHERE patient_presc_id IN ( :patientPrescIds ) GROUP BY patient_presc_id)";

  /**
   * gets the scheduler details for pending prescription dashboard.
   * 
   * @param prescIdsList
   *          the list of prescIds
   * @return list of map
   */
  public List<Map<String, Object>> getScheduleDetails(List<Long> prescIdsList) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("patientPrescIds", prescIdsList);
    List<BasicDynaBean> beanList = DatabaseHelper
        .queryToDynaList(GET_SCHEDULE_DETAILS_FOR_PENDING_PRESC, parameters);
    return ConversionUtils.copyListDynaBeansToMap(beanList);
  }

  /**
   * Gets appointment id's.
   */
  private static final String GET_APPOINTMENT_IDS = 
      "SELECT appointment_id from scheduler_appointments "
      + " WHERE appointment_status = 'Booked' AND appointment_time BETWEEN ? AND ? "
      + " AND patient_contact LIKE ? ";

  /**
   * Query to updates appointment status using received sms.
   */
  private static final String UPDATE_APPOINTMENT = "UPDATE scheduler_appointments "
      + "SET appointment_status = :status, changed_by = :changedBy, changed_time = now() ";

  /**
   * Gets appointment beans on status and appointment date.
   * @param paramMap the map
   * @return the list of beans
   */
  public List<BasicDynaBean> getAppointmentBeans(Map<String, Object> paramMap) {
    return DatabaseHelper.queryToDynaList(GET_APPOINTMENT_IDS, paramMap.get("fromDate"),
        paramMap.get("toDate"), "%" + StringUtils.trimToEmpty(paramMap.get("mnumber").toString()));
  }
  
  /**
   * Update Appointment Status.
   * 
   * @param paramMap     the map
   * @param appointments the list of beans
   */
  public void updateAppointmentStatus(Map<String, Object> paramMap,
      List<BasicDynaBean> appointments) {

    String appointmentStatus = paramMap.get("status").toString();

    if (appointments != null && !appointments.isEmpty()) {
      List<Integer> appointmentIds = new ArrayList<>();
      List<BasicDynaBean> updateLogBeanList = new ArrayList<BasicDynaBean>();
      BasicDynaBean updateLogBean = null;
      int appointmentId;
      StringBuilder updateQuery = new StringBuilder(UPDATE_APPOINTMENT);
      MapSqlParameterSource parameters = new MapSqlParameterSource();
      parameters.addValue("status", appointmentStatus);
      parameters.addValue("changedBy", "InstaAPI");
      if (appointmentStatus.equals("Cancel")) {
        updateQuery.append(", cancel_reason = 'Cancelled by Patient through SMS' ");
        updateQuery.append(", cancel_type = 'Patient' ");
      }
      for (BasicDynaBean bean : appointments) {
        appointmentId = (int) bean.get("appointment_id");
        appointmentIds.add(appointmentId);
        updateLogBean = instaSmsUpdatedAppointmentLogRepository.getBean();
        updateLogBean.set("id",
            Long.valueOf(instaSmsUpdatedAppointmentLogRepository.getNextSequence()));
        updateLogBean.set("appointment_id", appointmentId);
        updateLogBean.set("received_message_id", paramMap.get("message_id"));
        updateLogBean.set("updated_status_to", appointmentStatus);
        updateLogBeanList.add(updateLogBean);
      }
      updateQuery.append(" WHERE appointment_id in (:appointmentIds)");
      parameters.addValue("appointmentIds", appointmentIds);

      DatabaseHelper.update(updateQuery.toString(), parameters);
      instaSmsUpdatedAppointmentLogRepository.batchInsert(updateLogBeanList);
    }
  }
  
  /** The Constant UPDATE_WAITLIST. */
  private static final String UPDATE_WAITLIST = "UPDATE scheduler_appointments "
      + "SET waitlist =? where appointment_id = ?";
  
  /**
   * Batch upgrade waitlist.
   *
   * @param updateParamsList the update params list
   */
  public void batchUpgradeWaitlist(List<Object[]> updateParamsList) {
    DatabaseHelper.batchUpdate(UPDATE_WAITLIST, updateParamsList);
  }

  private static final String GET_APPOINTMENT_CLIENT = "select apc.* from api_clients apc"
      + " join appointment_source_master asm on (asm.client_id = apc.client_id )"
      + " join scheduler_appointments sa on (sa.app_source_id = asm.appointment_source_id) "
      + " where sa.appointment_id = ? ";

  private static final String GET_PATIENT_DATA_FOR_APPOINTMENT = "select "
      + " coalesce(pd.patient_name,cont.patient_name)  as first_name,"
      + " coalesce(pd.middle_name,cont.middle_name) as middle_name, "
      + " coalesce(pd.last_name,cont.last_name) as last_name,"
      + " coalesce(pd.salutation, cont.salutation_name) as salutation,"
      + " sa.patient_contact as patient_phone,"
      + " coalesce(pd.dateofbirth, cont.patient_dob) as dateofbirth,"
      + " coalesce(get_patient_age(pd.dateofbirth,pd.expected_dob),cont.patient_age) as age,"
      + " coalesce(cont.patient_age_units,get_patient_age_in(pd.dateofbirth, pd.expected_dob)"
      + " ) as age_in, coalesce(pd.email_id,cont.patient_email_id) as email,"
      + " pd.expected_dob as expected_dob, pd.government_identifier as government_identifier, "
      + " pd.identifier_id as identifier_id, pd.name_local_language as fourth_name, "
      + " coalesce(pd.patient_gender,cont.patient_gender) as patient_gender, "
      + " sa.mr_no, sa.contact_id from scheduler_appointments sa "
      + " LEFT JOIN contact_details cont ON (cont.contact_id = sa.contact_id ) "
      + " LEFT JOIN patient_details pd ON (pd.mr_no = sa.mr_no)  where sa.appointment_id = ?";

  public BasicDynaBean getPatientDetailsForAppointment(Integer appointmentId) {
    return DatabaseHelper.queryToDynaBean(GET_PATIENT_DATA_FOR_APPOINTMENT,
        new Object[] { appointmentId });
  }

  private static final String GET_CLIENTS_DETAILS = "select ac.client_id, ac.hash, ac.callback_url,"
      + " ac.retry_duration_min, ac.retry_count, ac.custom_header from api_clients AS ac "
      + " Left join event_push_subscribers_http  epsj on (ac.client_id = epsj.client_id) "
      + " where  epsj.event= :eventId";

  /**
   * Gets the client details.
   *
   * @param eventId the event id
   * @return the client details
   */
  public List<BasicDynaBean> getClientDetails(String eventId) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("eventId", eventId);
    return DatabaseHelper.queryToDynaList(GET_CLIENTS_DETAILS,parameters);
  }

  private static final String GET_SUBSCRIBER_DETAILS = " SELECT eps.include_filter, "
      + " eps.exclude_filter from event_push_subscribers_http as eps where event= :event "
      + " AND  client_id = :client_id";

  /**
   * Gets the subscriber details for event.
   *
   * @param eventId the event id
   * @param clientId the client id
   * @return the subscriber details for event
   */
  public Map<String, Object> getSubscriberDetailsForEvent(String eventId, int clientId) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("event", eventId);
    parameters.addValue("client_id", clientId);
    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(GET_SUBSCRIBER_DETAILS, parameters);
    if (bean != null && !bean.equals("")) {
      return bean.getMap();
    }
    return null;
  }

}

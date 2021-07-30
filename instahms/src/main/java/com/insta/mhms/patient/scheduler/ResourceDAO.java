package com.insta.mhms.patient.scheduler;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.resourcescheduler.ResourceBO.AppointMentResource;
import com.insta.hms.resourcescheduler.ResourceBO.Appointments;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

public class ResourceDAO extends GenericDAO {

  public ResourceDAO() {
    super("scheduler_appointments");
  }

  static Logger logger = LoggerFactory.getLogger(ResourceDAO.class);

  public static String GET_RESOURCE_AVAILABILITIES_LIST =
      "SELECT * FROM sch_resource_availability sra JOIN "
          + " sch_resource_availability_details srad ON (srad.res_avail_id = sra.res_avail_id) "
          + " WHERE  res_sch_type = ? AND res_sch_name = ? AND availability_date=? "
          + " AND from_time != to_time # order by srad.from_time";

  public static String WHERE_CLAUSE = "AND availability_status = ? ";

  /**
   * Get Resource Availabilities.
   *
   * @param category category paramter
   * @param availDate available date paramter
   * @param resourceName resource name parameter
   * @param status status paramter
   * @return returns list
   * @throws Exception throws Generic exception
   */
  public List getResourceAvailabilities(
      String category, Date availDate, String resourceName, String status) throws Exception {
    Connection con = null;
    PreparedStatement ps = null;
    int index = 1;
    try {
      con = DataBaseUtil.getConnection();
      String query = null;
      if (status != null) {
        query = GET_RESOURCE_AVAILABILITIES_LIST.replace("#", WHERE_CLAUSE);
      } else {
        query = GET_RESOURCE_AVAILABILITIES_LIST.replace("#", "");
      }
      ps = con.prepareStatement(query);
      ps.setString(index++, category);
      ps.setString(index++, resourceName);
      ps.setDate(index++, new java.sql.Date(availDate.getTime()));
      if (status != null && !status.equals("")) {
        ps.setString(index++, status);
      }
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public static String GET_DEFAULT_RESOURCE_AVAILABILITIES =
      "SELECT * FROM scheduler_master sm JOIN sch_default_res_availability_details sdra ON "
          + "(sdra.res_sch_id = sm.res_sch_id) WHERE day_of_week = ? AND sm.res_sch_name = ? AND "
          + " res_sch_type = ? AND from_time != to_time AND status = 'A' # order by sdra.from_time";

  /**
   * Get Resource Default Availabilities.
   *
   * @param resource resource parameter
   * @param dayOfWeek day of week parameter
   * @param category category parameter
   * @param status status parameter
   * @return returns list parameter
   * @throws Exception throws Generic Exception
   */
  public List getResourceDefaultAvailabilities(
      String resource, int dayOfWeek, String category, String status) throws Exception {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      if (status != null) {
        GET_DEFAULT_RESOURCE_AVAILABILITIES =
            GET_DEFAULT_RESOURCE_AVAILABILITIES.replace("#", "AND availability_status = ?");
      } else {
        GET_DEFAULT_RESOURCE_AVAILABILITIES = GET_DEFAULT_RESOURCE_AVAILABILITIES.replace("#", "");
      }
      ps = con.prepareStatement(GET_DEFAULT_RESOURCE_AVAILABILITIES);
      ps.setInt(1, dayOfWeek);
      ps.setString(2, resource);
      ps.setString(3, category);
      if (status != null) {
        ps.setString(4, status);
      }
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String GET_RESOURCE_LIST =
      " select sm.res_sch_id, sm.res_sch_category, sm.res_sch_type, sm.dept, sm.res_sch_name, "
          + " sim.resource_type, sim.resource_id, sm.default_duration, sm.height_in_px, "
          + " sm.description from scheduler_master sm left join scheduler_item_master sim  "
          + " using(res_sch_id) where sm.res_sch_name = ? AND sm.res_sch_type = ? "
          + " AND status = 'A' ";

  /**
   * Get resouce list by resource type.
   *
   * @param category category parameter
   * @param resourceScheduleName resource schedule name
   * @return returns list of BasicDynaBean
   * @throws SQLException throws Sql Exception
   */
  public static List<BasicDynaBean> getResourceListByResourceType(
      String category, String resourceScheduleName) throws SQLException {
    List<BasicDynaBean> list = null;
    PreparedStatement ps = null;
    Connection con = DataBaseUtil.getReadOnlyConnection();
    try {
      logger.debug(GET_RESOURCE_LIST);
      ps = con.prepareStatement(GET_RESOURCE_LIST);
      ps.setString(1, resourceScheduleName);
      ps.setString(2, category);
      list = DataBaseUtil.queryToDynaList(ps);
      if (list != null && list.size() > 0) {
        //do nothing
      } else {
        ps = con.prepareStatement(GET_RESOURCE_LIST);
        ps.setString(1, "*");
        ps.setString(2, category);
        list = DataBaseUtil.queryToDynaList(ps);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  private static final String GET_SCHEDULABLE_DOCTORS =
      "select * from doctors where status = 'A' AND schedule = true order by doctor_name";

  /**
   * Get all schedulable doctors.
   *
   * @return returns list of BasicDynaBean
   * @throws SQLException may throw Sql Exception
   */
  public static List<BasicDynaBean> getAllSchedulableDoctors() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_SCHEDULABLE_DOCTORS);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String GET_APPOINTMENT_DETAILS =
      "select * from scheduler_appointments where res_sch_id = 1 AND prim_res_id = ? # ";

  /**
   * Get appointment Details.
   *
   * @param resourceId resource id
   * @param appointmentTime appointment time
   * @return returns BasicDynaBean
   * @throws SQLException may throw Sql Exception
   */
  public static BasicDynaBean getAppointmentDetails(String resourceId, Timestamp appointmentTime)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    String query = new String(GET_APPOINTMENT_DETAILS);

    try {
      con = DataBaseUtil.getConnection();
      query = query.replace("#", " AND appointment_time = ? ");
      ps = con.prepareStatement(query);
      ps.setString(1, resourceId);
      ps.setTimestamp(2, appointmentTime);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Get appointment Details.
   *
   * @param resourceId resource id
   * @param appointmentDate appointment date
   * @return returns BasicDynaBean
   * @throws SQLException may throw Sql Exception
   */
  public static List<BasicDynaBean> getAppointmentDetails(String resourceId, Date appointmentDate)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    String query = new String(GET_APPOINTMENT_DETAILS);
    try {
      con = DataBaseUtil.getConnection();
      query = query.replace("#", " AND date(appointment_time) = ? ");
      ps = con.prepareStatement(query);
      ps.setString(1, resourceId);
      ps.setDate(2, new java.sql.Date(appointmentDate.getTime()));
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public static final String INSERT_APPOINTMENT =
      "INSERT INTO scheduler_appointments (mr_no, patient_name, patient_contact, "
          + " complaint, appointment_id, res_sch_id, res_sch_name, appointment_time, "
          + " duration, appointment_status, booked_by, booked_time, cancel_reason, visit_id, "
          + " consultation_type_id, remarks,changed_by,scheduler_visit_type,"
          + " scheduler_prior_auth_no,scheduler_prior_auth_mode_id,center_id,"
          + " unique_appt_ind, prim_res_id) "
          + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?, ?, ?, ?)";

  /**
   * Insert a appointment.
   *
   * @param con connection object
   * @param appt appintment object
   * @return returns true or false
   * @throws SQLException may throw Sql Exception
   */
  public static boolean insertAppointments(Connection con, Appointments appt) throws SQLException {

    PreparedStatement ps = null;
    boolean success = true;
    try {
      ps = con.prepareStatement(INSERT_APPOINTMENT);
      ps.setString(1, appt.getMrNo());
      ps.setString(2, appt.getPatientName());
      ps.setString(3, appt.getPhoneNo());
      ps.setString(4, appt.getComplaint());
      ps.setInt(5, appt.getAppointmentId());
      ps.setInt(6, appt.getScheduleId());
      ps.setString(7, appt.getScheduleName());
      ps.setTimestamp(8, appt.getAppointmentTime());
      ps.setInt(9, appt.getAppointmentDuration());
      ps.setString(10, appt.getAppointStatus());
      ps.setString(11, appt.getBookedBy());
      ps.setTimestamp(12, appt.getBookedTime());
      ps.setString(13, appt.getCancelReason());
      ps.setString(14, appt.getVisitId());
      ps.setInt(15, appt.getConsultationTypeId());
      ps.setString(16, appt.getRemarks());
      ps.setString(17, appt.getChangedBy());
      ps.setString(18, appt.getSchedulerVisitType());
      ps.setString(19, appt.getSchPriorAuthId());
      ps.setInt(20, appt.getSchPriorAuthModeId());
      ps.setInt(21, appt.getCenterId());
      ps.setInt(22, appt.getUnique_appt_ind());
      ps.setString(23, appt.getPrim_res_id());
      success = ps.executeUpdate() > 0;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
    return success;
  }

  /**
   * Insert appointments.
   *
   * @param con connection object
   * @param list list of appintment objects
   * @return returns true or false
   * @throws SQLException may throw Sql Exception
   */
  public static boolean insertAppointments(Connection con, List list) throws SQLException {

    PreparedStatement ps = con.prepareStatement(INSERT_APPOINTMENT);
    boolean success = true;
    Iterator iterator = list.iterator();
    while (iterator.hasNext()) {
      Appointments appt = (Appointments) iterator.next();
      ps.setString(1, appt.getMrNo());
      ps.setString(2, appt.getPatientName());
      ps.setString(3, appt.getPhoneNo());
      ps.setString(4, appt.getComplaint());
      ps.setInt(5, appt.getAppointmentId());
      ps.setInt(6, appt.getScheduleId());
      ps.setString(7, appt.getScheduleName());
      ps.setTimestamp(8, appt.getAppointmentTime());
      ps.setInt(9, appt.getAppointmentDuration());
      ps.setString(10, appt.getAppointStatus());
      ps.setString(11, appt.getBookedBy());
      ps.setTimestamp(12, appt.getBookedTime());
      ps.setString(13, appt.getCancelReason());
      ps.setString(14, appt.getVisitId());
      ps.setInt(15, appt.getConsultationTypeId());
      ps.setString(16, appt.getRemarks());
      ps.setString(17, appt.getChangedBy());
      ps.setString(18, appt.getSchedulerVisitType());
      ps.setString(19, appt.getSchPriorAuthId());
      ps.setInt(20, appt.getSchPriorAuthModeId());
      ps.setInt(21, appt.getCenterId());
      ps.setInt(22, appt.getUnique_appt_ind());
      ps.setString(23, appt.getPrim_res_id());
      ps.addBatch();
    }
    int[] results = ps.executeBatch();

    ps.close();
    for (int p = 0; p < results.length; p++) {
      if (results[p] <= 0) {
        success = false;
        break;
      }
    }
    return success;
  }

  public static final String INSERT_APPOINTMENT_ITEMS =
      "INSERT INTO scheduler_appointment_items (appointment_id, resource_type,"
          + " resource_id, appointment_item_id,user_name,mod_time) values (?, ?, ?, ?, ?, ?)";

  /**
   * Insert appointment items.
   *
   * @param con connection object
   * @param list list of appointments
   * @return returns true or false
   * @throws SQLException may throw Sql Exception
   */
  public static boolean insertAppointmentItems(Connection con, List list) throws SQLException {
    PreparedStatement ps = con.prepareStatement(INSERT_APPOINTMENT_ITEMS);
    boolean success = true;
    Iterator iterator = list.iterator();
    while (iterator.hasNext()) {
      AppointMentResource res = (AppointMentResource) iterator.next();
      ps.setInt(1, res.appointmentId);
      ps.setString(2, res.getResourceType());
      ps.setString(3, res.getResourceId());
      ps.setInt(4, res.getAppointment_item_id());
      ps.setString(5, res.getUser_name());
      ps.setTimestamp(6, res.getMod_time());
      ps.addBatch();
    }
    int[] results = ps.executeBatch();

    ps.close();
    for (int p = 0; p < results.length; p++) {
      if (results[p] <= 0) {
        success = false;
        break;
      }
    }
    return success;
  }

  private static final String GET_CENTERWISE_SCHEDULABLE_DOCTORS =
      "select * from doctors d"
          + " LEFT JOIN doctor_center_master dcm ON(dcm.doctor_id=d.doctor_id)"
          + " WHERE d.status = 'A' AND schedule = true order by doctor_name";

  /**
   * Get center wise schedulable doctors.
   *
   * @return returns BasicDynaBean
   * @throws SQLException may throw Sql Exception
   */
  public static List<BasicDynaBean> getCenterWiseAllSchedulableDoctors() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_CENTERWISE_SCHEDULABLE_DOCTORS);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public static final String GET_CENTERWISE_SCHEDULE_LIST_DOCTER =
      " select d.doctor_id , d.doctor_name, d.specialization,"
          + " d.doctor_type, d.doctor_address, d.doctor_mobile, d.doctor_mail_id, "
          + " d.op_consultation_validity, d.dept_id, d.ot_doctor_flag, d.consulting_doctor_flag, "
          + " d.schedule, d.qualification, d.registration_no, d.res_phone, d.clinic_phone, "
          + " d.payment_category, d.payment_eligible, d.doctor_license_number, "
          + " d.allowed_revisit_count, d.custom_field1_value, d.custom_field2_value, "
          + " d.custom_field3_value,d.custom_field4_value, d.custom_field5_value, "
          + " d.ip_discharge_consultation_validity, d.ip_discharge_consultation_count, "
          + " d.ip_template_id, d.overbook_limit, to_char(d.created_timestamp AT TIME ZONE "
          + " (SELECT  current_setting('TIMEZONE')) AT TIME ZONE 'UTC', "
          + " 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS created_timestamp,"
          + " to_char(d.updated_timestamp AT TIME ZONE "
          + " (SELECT  current_setting('TIMEZONE')) AT TIME ZONE 'UTC', "
          + " 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS updated_timestamp,"
          + " d.practition_type, d.status as doctor_status, dcm.status as doctor_center_status, "
          + " dcm.doc_center_id, dcm.center_id from "
          + " doctors d LEFT JOIN doctor_center_master dcm ON(dcm.doctor_id=d.doctor_id) "
          + " join hospital_center_master hcm on(dcm.center_id=hcm.center_id) "
          + " WHERE @ hcm.status='A' AND d.scheduleable_by = 'A' order by ? ";

  /**
   * Get center wise schedulable doctors.
   *
   * @param sortcolumn sort column
   * @param status status
   * @return returns BasicDynaBean
   * @throws SQLException may throw Sql Exception
   */
  public static List<BasicDynaBean> getCenterWiseAllSchedulableDoctors(
      String sortcolumn, String status) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      String query = new String(GET_CENTERWISE_SCHEDULE_LIST_DOCTER);
      if (status == null || status.trim().length() == 0) {
        query = query.replace("@", "d.status = 'A' AND dcm.status = 'A' AND ");
      } else {
        query = query.replace("@", "");
      }
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(query);
      ps.setString(1, sortcolumn);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String CHECK_IS_SLOT_IS_BOOKED =
      "SELECT * FROM "
          + " (SELECT *, appointment_time +(duration||' mins')::interval AS end_appointment_time "
          + " FROM scheduler_appointments sa "
          + " JOIN scheduler_appointment_items sai using(appointment_id)) as foo"
          + " WHERE ("
          + " (appointment_time <= ? AND end_appointment_time > ?) "
          + "  OR (appointment_time >= ? AND appointment_time < ?) "
          + " )"
          + " AND appointment_status NOT IN('Cancel','Noshow')"
          + " AND appointment_id != ? AND foo.resource_type in(?, ?) AND foo.resource_id = ? "
          + " ORDER BY end_appointment_time ";

  /**
   * method to check if slot is booked.
   *
   * @param statrApptTime appointment start time
   * @param endApptTime appointment end time
   * @param resName resource name
   * @param appointmentId appointment id
   * @param primaryResource primary resource
   * @return returns BasicDynaBean
   * @throws SQLException may throw Sql Exception
   */
  public static List<BasicDynaBean> isSlotBooked(
      Timestamp statrApptTime,
      Timestamp endApptTime,
      String resName,
      String appointmentId,
      String primaryResource)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    int index = 1;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(CHECK_IS_SLOT_IS_BOOKED);
      ps.setTimestamp(index++, statrApptTime);
      ps.setTimestamp(index++, statrApptTime);
      ps.setTimestamp(index++, statrApptTime);
      ps.setTimestamp(index++, endApptTime);
      if (appointmentId != null && !appointmentId.equals("") && !appointmentId.equals("No")) {
        ps.setInt(index++, Integer.parseInt(appointmentId));
      } else {
        ps.setInt(index++, -1);
      }
      ps.setString(index++, "DOC");
      ps.setString(index++, "OPDOC");
      ps.setString(index++, primaryResource);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      com.insta.instaapi.common.DbUtil.closeConnections(null, ps);
    }
  }

  public static final String GET_SLOT_FOR_PATIENT =
      "SELECT * FROM"
          + "(SELECT *,appointment_time+(duration||' mins')::interval AS end_appointment_time "
          + " FROM scheduler_appointments sa )as foo"
          + " WHERE ("
          + "(appointment_time <= ? AND end_appointment_time > ?) "
          + "OR (appointment_time >= ? AND appointment_time < ?) "
          + "   )"
          + " AND appointment_status NOT IN('Cancel','Noshow') "
          + " AND appointment_id != ?"
          + " # "
          + " ORDER BY end_appointment_time ";

  /**
   * Check if appointment exists.
   *
   * @param statrApptTime appointment start time
   * @param endApptTime appointment end time
   * @param appointmentId appointment id
   * @param mrno MR number
   * @param patientName patient name
   * @param mobileNo mobile number
   * @return returns BasicDynaBean
   * @throws SQLException may throw Sql Exception
   */
  public static List<BasicDynaBean> appointmentExists(
      Timestamp statrApptTime,
      Timestamp endApptTime,
      int appointmentId,
      String mrno,
      String patientName,
      String mobileNo)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    int index = 1;
    String query = new String(GET_SLOT_FOR_PATIENT);
    if (mrno.equals("")) {
      query = query.replace("#", "AND patient_name = ? AND patient_contact = ?");
    } else {
      query = query.replace("#", "AND mr_no = ? ");
    }
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(query);
      ps.setTimestamp(index++, statrApptTime);
      ps.setTimestamp(index++, statrApptTime);
      ps.setTimestamp(index++, statrApptTime);
      ps.setTimestamp(index++, endApptTime);
      if (!mrno.equals("")) {
        ps.setInt(index++, appointmentId);
        ps.setString(index++, mrno);
      } else {
        ps.setInt(index++, appointmentId);
        ps.setString(index++, patientName);
        ps.setString(index++, mobileNo);
      }

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      com.insta.instaapi.common.DbUtil.closeConnections(null, ps);
    }
  }
}
